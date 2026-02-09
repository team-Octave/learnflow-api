package com.teamexp.learnflowapi.payment.model.constant;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    DONE("결제 성공"),
    CANCELED("결제 취소"),
    ABORTED("결제 실패");

    private final String result;

    PaymentStatus(String result) {
        this.result = result;
    }

}
