package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LessonAccessDeniedException extends BaseException {
    public LessonAccessDeniedException() {
        super(ErrorCode.LESSON_ACCESS_DENIED);
    }
    
}
