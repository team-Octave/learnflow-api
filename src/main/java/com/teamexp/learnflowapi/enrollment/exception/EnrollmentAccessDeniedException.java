package com.teamexp.learnflowapi.enrollment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class EnrollmentAccessDeniedException extends BaseException {
    public EnrollmentAccessDeniedException() {
        super(ErrorCode.FORBIDDEN);
    }
}
