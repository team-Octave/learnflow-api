package com.teamexp.learnflowapi.payment.service.dto;

import com.teamexp.learnflowapi.payment.model.constant.PaymentStatus;

public record PaymentDto(
        String paymentKey,
        String type,
        String orderId,
        String orderName,
        String method,
        Long totalAmount,
        PaymentStatus status,
        String approvedAt,
        TossFailureDto tossFailureDto
) {
}
