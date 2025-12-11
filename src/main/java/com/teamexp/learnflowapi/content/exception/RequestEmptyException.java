package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class RequestEmptyException extends BaseException {
    public RequestEmptyException( ) {
        super(ErrorCode.REQUEST_EMPTY);
    }
}
