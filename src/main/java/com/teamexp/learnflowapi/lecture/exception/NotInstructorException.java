package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class NotInstructorException extends BaseException {
    public NotInstructorException() {
        super(ErrorCode.NOT_INSTRUCTOR);
    }
}
