package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureInstructorUnauthorizedException extends BaseException {
    public LectureInstructorUnauthorizedException() {
        super(ErrorCode.LECTURE_INSTRUCTOR_UNAUTHORIZED);
    }
    
}
