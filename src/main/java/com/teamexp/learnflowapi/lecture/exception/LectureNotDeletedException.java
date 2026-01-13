package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureNotDeletedException extends BaseException {
    public LectureNotDeletedException() {
        super(ErrorCode.LECTURE_NOT_DELETED);
    }
    
}
