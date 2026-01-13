package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LessonQuizCountInvalidException extends BaseException {
    public LessonQuizCountInvalidException() {
        super(ErrorCode.LESSON_QUIZ_COUNT_INVALID);
    }
}

