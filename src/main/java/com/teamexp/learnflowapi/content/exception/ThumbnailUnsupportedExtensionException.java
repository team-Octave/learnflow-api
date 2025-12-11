package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class ThumbnailUnsupportedExtensionException extends BaseException {
    public ThumbnailUnsupportedExtensionException() {
        super(ErrorCode.THUMBNAIL_UNSUPPORTED_EXTENSION);
    }
}
