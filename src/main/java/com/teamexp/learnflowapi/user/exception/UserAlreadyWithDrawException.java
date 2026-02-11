package com.teamexp.learnflowapi.user.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class UserAlreadyWithDrawException extends BaseException {

    public UserAlreadyWithDrawException() {
        super(ErrorCode.EMAIL_DUPLICATED);
    }
}
