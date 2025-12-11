package com.teamexp.learnflowapi.enrollment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class SelfEnrollmentNotAllowedException extends BaseException {
    public SelfEnrollmentNotAllowedException() {
        super(ErrorCode.SELF_ENROLLMENT_NOT_ALLOWED);
    }
}
