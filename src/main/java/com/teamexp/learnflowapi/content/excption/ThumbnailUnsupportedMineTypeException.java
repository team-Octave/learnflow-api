package com.teamexp.learnflowapi.content.excption;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class ThumbnailUnsupportedMineTypeException extends BaseException {
    public ThumbnailUnsupportedMineTypeException() {
        super(ErrorCode.THUMBNAIL_UNSUPPORTED_MIME_TYPE);
    }
}
