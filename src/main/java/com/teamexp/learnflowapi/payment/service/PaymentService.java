package com.teamexp.learnflowapi.payment.service;

import com.teamexp.learnflowapi.payment.dto.request.PaymentConfirmRequest;
import com.teamexp.learnflowapi.payment.dto.response.PaymentConfirmResponse;
import com.teamexp.learnflowapi.payment.exception.TossErrorException;
import com.teamexp.learnflowapi.payment.model.PaymentHistory;
import com.teamexp.learnflowapi.payment.model.constant.PlanType;
import com.teamexp.learnflowapi.payment.repository.PaymentHistoryRepository;
import com.teamexp.learnflowapi.payment.service.dto.PaymentDto;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final RestClient tossRestClient;
    private final TossProps tossProps;
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Transactional
    public PaymentConfirmResponse tossConfirm(PaymentConfirmRequest request, String userId) {
        PaymentDto paymentDto = callTossConfirmApi(request);

        PlanType planType = resolvePlanType(paymentDto.orderName());

        savePaymentHistory(userId, paymentDto, planType);

        return PaymentConfirmResponse.from(paymentDto);
    }

    private PaymentDto callTossConfirmApi(PaymentConfirmRequest request) {
        String encodedKey = Base64.getEncoder()
                .encodeToString((tossProps.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8));

        return tossRestClient.post()
                .uri(tossProps.getConfirmUrl())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new TossErrorException();
                })
                .body(PaymentDto.class);
    }

    private void savePaymentHistory(String userId, PaymentDto dto, PlanType planType) {
        PaymentHistory history = PaymentHistory.create(
                userId,
                dto.orderId(),
                dto.paymentKey(),
                dto.totalAmount(),
                planType,
                LocalDateTime.parse(dto.approvedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
        paymentHistoryRepository.save(history);
    }

    private PlanType resolvePlanType(String orderName) {
        if (orderName.contains("1개월")) {
            return PlanType.ONE_MONTH;
        }
        if (orderName.contains("3개월")) {
            return PlanType.THREE_MONTHS;
        }
        if (orderName.contains("6개월")) {
            return PlanType.HALF_YEAR;
        }
        if (orderName.contains("12개월")) {
            return PlanType.YEAR;
        }
        return PlanType.ONE_MONTH;
    }
}
