package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LessonVideoUrlInvalidException extends BaseException {
    public LessonVideoUrlInvalidException() {
        super(ErrorCode.LESSON_VIDEO_URL_INVALID);
    }
}

