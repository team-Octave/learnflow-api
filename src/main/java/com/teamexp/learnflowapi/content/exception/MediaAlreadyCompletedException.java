package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class MediaAlreadyCompletedException extends BaseException {
    public MediaAlreadyCompletedException() {
        super(ErrorCode.MEDIA_ALREADY_COMPLETED);
    }
}
