package com.teamexp.learnflowapi.payment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class InvalidPlanTypeException extends BaseException {
    public InvalidPlanTypeException() {
        super(ErrorCode.INVALID_PLAN_TYPE);
    }
}