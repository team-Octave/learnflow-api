package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class ThumbnailUnsupportedMimeTypeException extends BaseException {
    public ThumbnailUnsupportedMimeTypeException() {
        super(ErrorCode.THUMBNAIL_UNSUPPORTED_MIME_TYPE);
    }
}
