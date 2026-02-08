package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class PaymentTypeInvalidException extends BaseException {
    public PaymentTypeInvalidException() {
        super(ErrorCode.PAYMENT_TYPE_INVALID);
    }
    
}
