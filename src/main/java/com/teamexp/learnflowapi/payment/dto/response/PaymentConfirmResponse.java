package com.teamexp.learnflowapi.payment.dto.response;

public record PaymentConfirmResponse(
        String orderId,
        String orderName,
        String method,
        Long totalAmount,
        String status,
        String approvedAt) {
}
