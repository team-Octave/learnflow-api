package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LessonVideoNotFoundException extends BaseException {
    public LessonVideoNotFoundException() {
        super(ErrorCode.LESSON_VIDEO_NOT_FOUND);
    }
}
