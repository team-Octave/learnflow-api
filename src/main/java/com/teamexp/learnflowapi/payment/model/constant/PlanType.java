package com.teamexp.learnflowapi.payment.model.constant;

import lombok.Getter;

@Getter
public enum PlanType {
    ONE_MONTH(1),
    THREE_MONTHS(3),
    HALF_YEAR(6),
    YEAR(12);

    private final int value;

    PlanType(int value) {
        this.value = value;
    }
}
