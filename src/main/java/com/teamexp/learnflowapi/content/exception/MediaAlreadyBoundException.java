package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class MediaAlreadyBoundException extends BaseException {
    public MediaAlreadyBoundException() {
        super(ErrorCode.MEDIA_ALREADY_BOUND);
    }
}
