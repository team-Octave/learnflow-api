package com.teamexp.learnflowapi.payment.service;

import com.teamexp.learnflowapi.payment.repository.PaymentHistoryRepository;
import com.teamexp.learnflowapi.payment.service.dto.PaymentQueryDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentHistoryService {
    private final PaymentHistoryRepository paymentHistoryRepository;

    public List<PaymentQueryDto> getAllPaymentHistoryByUserId(String userId) {
        return paymentHistoryRepository.findByUserIdOrderByApprovedAtDesc(userId).stream().map(PaymentQueryDto::from).toList();
    }

}
