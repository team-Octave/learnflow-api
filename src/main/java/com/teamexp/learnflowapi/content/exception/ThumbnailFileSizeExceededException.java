package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class ThumbnailFileSizeExceededException extends BaseException {
    public ThumbnailFileSizeExceededException() {
        super(ErrorCode.THUMBNAIL_FILE_SIZE_EXCEEDED);
    }
}
