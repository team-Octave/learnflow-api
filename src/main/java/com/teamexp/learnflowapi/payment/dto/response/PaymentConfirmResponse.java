package com.teamexp.learnflowapi.payment.dto.response;

import com.teamexp.learnflowapi.payment.service.dto.PaymentDto;

public record PaymentConfirmResponse(
        String orderId,
        Long amount,
        String status
) {
    public static PaymentConfirmResponse from(PaymentDto paymentDto) {
        return  new PaymentConfirmResponse(
                paymentDto.orderId(),
                paymentDto.totalAmount(),
                paymentDto.status().getResult()
        );
    }
}
