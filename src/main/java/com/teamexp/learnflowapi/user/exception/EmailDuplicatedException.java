package com.teamexp.learnflowapi.user.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class EmailDuplicatedException extends BaseException {

    public EmailDuplicatedException() {
        super(ErrorCode.EMAIL_DUPLICATED);
    }
}
