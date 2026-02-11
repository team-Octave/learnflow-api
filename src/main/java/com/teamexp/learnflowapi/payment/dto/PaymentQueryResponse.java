package com.teamexp.learnflowapi.payment.dto;

import com.teamexp.learnflowapi.payment.service.dto.PaymentQueryDto;
import java.time.Instant;

public record PaymentQueryResponse(
        Long id,
        Instant paymentDate,
        String planType,
        Long amount,
        String status
) {
    public static PaymentQueryResponse of(PaymentQueryDto dto){
        return new PaymentQueryResponse(dto.id(), dto.paymentDate(), dto.planType().getValue() +"개월", dto.amount(), dto.status().getResult());
    }
}
