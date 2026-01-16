package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LessonAlreadyBoundToMediaException extends BaseException {
    public LessonAlreadyBoundToMediaException() {
        super(ErrorCode.LESSON_ALREADY_BOUND_TO_MEDIA);
    }
}
