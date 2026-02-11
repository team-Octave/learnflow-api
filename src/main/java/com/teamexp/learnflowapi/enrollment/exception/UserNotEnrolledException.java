package com.teamexp.learnflowapi.enrollment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class UserNotEnrolledException extends BaseException {
    public UserNotEnrolledException() {
        super(ErrorCode.USER_NOT_ENROLLED);
    }
}
