package com.teamexp.learnflowapi.enrollment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class CompletedLessonAlreadyExistsException extends BaseException {
    public CompletedLessonAlreadyExistsException() {
        super(ErrorCode.COMPLETED_LESSON_ALREADY_EXISTS);
    }
}
