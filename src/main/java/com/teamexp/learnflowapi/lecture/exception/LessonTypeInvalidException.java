package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LessonTypeInvalidException extends BaseException {
    public LessonTypeInvalidException() {
        super(ErrorCode.LESSON_TYPE_INVALID);
    }
}
