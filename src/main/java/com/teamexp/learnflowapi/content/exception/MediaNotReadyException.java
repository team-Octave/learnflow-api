package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class MediaNotReadyException extends BaseException {
    public MediaNotReadyException() {
        super(ErrorCode.MEDIA_NOT_READY);
    }
}
