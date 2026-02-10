package com.teamexp.learnflowapi.ai.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class AiException extends BaseException {
    public AiException(ErrorCode errorCode) {
        super(errorCode);
    }
}
