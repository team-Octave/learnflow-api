package com.teamexp.learnflowapi.payment.service.dto;

import com.teamexp.learnflowapi.payment.model.PaymentHistory;
import com.teamexp.learnflowapi.payment.model.constant.PaymentStatus;
import com.teamexp.learnflowapi.payment.model.constant.PlanType;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Builder;

public record PaymentQueryDto(
        Long id,
        Long amount,
        PlanType planType,
        PaymentStatus status,
        Instant paymentDate
) {
    @Builder
    public PaymentQueryDto(Long id, Long amount, PlanType planType, PaymentStatus status, Instant paymentDate) {
        this.id = id;
        this.amount = amount;
        this.planType = planType;
        this.status = status;
        this.paymentDate = paymentDate;
    }

    public static PaymentQueryDto from(PaymentHistory paymentHistory) {
        return PaymentQueryDto.builder()
                .id(paymentHistory.getId())
                .amount(paymentHistory.getAmount())
                .planType(paymentHistory.getPlanType())
                .status(paymentHistory.getStatus())
                .paymentDate(paymentHistory.getApprovedAt())
                .build();
    }
}
