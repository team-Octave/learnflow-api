package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class VideoUrlEmptyException extends BaseException {
    public VideoUrlEmptyException( ) {
        super(ErrorCode.VIDEO_URL_EMPTY);
    }
}
