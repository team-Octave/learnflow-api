package com.teamexp.learnflowapi.enrollment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class EnrollmentAlreadyExistsException extends BaseException {


    public EnrollmentAlreadyExistsException() {
        super(ErrorCode.ENROLLMENT_ALREADY_EXISTS);
    }
}
