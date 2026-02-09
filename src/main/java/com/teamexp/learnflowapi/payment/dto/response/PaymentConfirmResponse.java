package com.teamexp.learnflowapi.payment.dto.response;

import com.teamexp.learnflowapi.payment.model.constant.PaymentStatus;
import com.teamexp.learnflowapi.payment.service.dto.PaymentDto;

public record PaymentConfirmResponse(
        String orderId,
        Long amount,
        PaymentStatus status
) {
    public static PaymentConfirmResponse from(PaymentDto paymentDto) {
        return  new PaymentConfirmResponse(
                paymentDto.orderId(),
                paymentDto.totalAmount(),
                paymentDto.status()
        );
    }
}
