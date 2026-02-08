package com.teamexp.learnflowapi.payment.service;

import com.github.loki4j.client.http.HttpHeaders;
import com.teamexp.learnflowapi.payment.dto.request.PaymentConfirmRequest;
import com.teamexp.learnflowapi.payment.dto.response.PaymentConfirmResponse;
import com.teamexp.learnflowapi.payment.exception.TossErrorException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final RestClient tossRestClient;
    @Value("${toss.payments.secret-key}")
    private String secretKey;
    @Value("${toss.payments.confirm-url}")
    private String confirmUrl;

    public PaymentConfirmResponse tossConfirm(PaymentConfirmRequest request) {
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return tossRestClient.post()
                .uri("/payments/confirm")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedKey)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new TossErrorException();
                })
                .body(PaymentConfirmResponse.class);
    }

}
