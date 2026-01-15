package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class MediaNotFoundException extends BaseException {
    public MediaNotFoundException() {
        super(ErrorCode.MEDIA_NOT_FOUND);
    }
}
