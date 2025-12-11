package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureThumbnailNotFoundException extends BaseException {
    public LectureThumbnailNotFoundException() {
        super(ErrorCode.LECTURE_THUMBNAIL_NOT_FOUND);
    }
}
