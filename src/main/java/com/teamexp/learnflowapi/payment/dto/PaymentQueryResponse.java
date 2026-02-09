package com.teamexp.learnflowapi.payment.dto;

import com.teamexp.learnflowapi.payment.service.dto.PaymentQueryDto;
import java.time.LocalDateTime;

public record PaymentQueryResponse(
        Long id,
        LocalDateTime paymentDate,
        String planType,
        String status
) {
    public static PaymentQueryResponse of(PaymentQueryDto dto){
        return new PaymentQueryResponse(dto.id(), dto.paymentDate(), dto.planType().getValue() +"개월", dto.status().getResult());
    }
}
