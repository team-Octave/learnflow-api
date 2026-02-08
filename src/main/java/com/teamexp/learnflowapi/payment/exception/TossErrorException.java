package com.teamexp.learnflowapi.payment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class TossErrorException extends BaseException {
    public TossErrorException() {
        super(ErrorCode.PAYMENT_CONFIRM_FAILED);
    }
}
