package com.teamexp.learnflowapi.enrollment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class EnrollmentNotFoundException extends BaseException {

    public EnrollmentNotFoundException() {
        super(ErrorCode.ENROLLMENT_NOT_FOUND);
    }
}
