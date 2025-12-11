package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureLevelInvalidException extends BaseException {
    public LectureLevelInvalidException() {
        super(ErrorCode.LECTURE_LEVEL_INVALID);
    }
    
}
