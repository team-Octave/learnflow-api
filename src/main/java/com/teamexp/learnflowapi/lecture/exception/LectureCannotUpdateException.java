package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureCannotUpdateException extends BaseException {
    public LectureCannotUpdateException() {
        super(ErrorCode.LECTURE_CANNOT_UPDATE);
    }
    
}
