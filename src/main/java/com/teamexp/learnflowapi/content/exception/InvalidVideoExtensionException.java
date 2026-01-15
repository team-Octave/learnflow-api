package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class InvalidVideoExtensionException extends BaseException {
    public InvalidVideoExtensionException() {
        super(ErrorCode.INVALID_VIDEO_EXTENSION);
    }
}
