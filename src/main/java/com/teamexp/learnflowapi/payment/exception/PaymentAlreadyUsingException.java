package com.teamexp.learnflowapi.payment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class PaymentAlreadyUsingException extends BaseException {
    public PaymentAlreadyUsingException() {
        super(ErrorCode.ALREADY_ON_MEMBERSHIP);
    }
}
