package com.teamexp.learnflowapi.user.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class NicknameDuplicateException extends BaseException {
    public NicknameDuplicateException() {
        super(ErrorCode.NICKNAME_DUPLICATED);
    }
}
