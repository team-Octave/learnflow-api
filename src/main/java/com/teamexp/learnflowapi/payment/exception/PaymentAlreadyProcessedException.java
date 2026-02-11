package com.teamexp.learnflowapi.payment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class PaymentAlreadyProcessedException extends BaseException {
    public PaymentAlreadyProcessedException() {
        super(ErrorCode.PAYMENT_ALREADY_PROCESSED);
    }
}
