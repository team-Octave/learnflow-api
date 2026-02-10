package com.teamexp.learnflowapi.payment.model.constant;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    WAITING_FOR_DEPOSIT("결제 대기"),
    IN_PROGRESS("결제 진행중"),
    PARTIAL_CANCELED("부분 취소"),
    EXPIRED("결제 만료"),
    DONE("결제 성공"),
    CANCELED("결제 취소"),
    ABORTED("결제 실패");

    private final String result;

    PaymentStatus(String result) {
        this.result = result;
    }

}
