package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureDeletedException extends BaseException {
    public LectureDeletedException() {
        super(ErrorCode.LECTURE_DELETED);
    }
    
}
