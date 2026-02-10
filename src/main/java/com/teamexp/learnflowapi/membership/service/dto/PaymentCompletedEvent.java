package com.teamexp.learnflowapi.membership.service.dto;

import com.teamexp.learnflowapi.payment.model.constant.PlanType;

public record PaymentCompletedEvent(
        String userId,
        PlanType planType
) {
}
