package com.teamexp.learnflowapi.user.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
