package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LessonNotFoundException extends BaseException {
    public LessonNotFoundException() {
        super(ErrorCode.LESSON_NOT_FOUND_IN_CHAPTER);
    }
    
}
