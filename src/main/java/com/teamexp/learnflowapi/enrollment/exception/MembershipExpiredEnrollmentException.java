package com.teamexp.learnflowapi.enrollment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class MembershipExpiredEnrollmentException extends BaseException {
    public MembershipExpiredEnrollmentException() {
        super(ErrorCode.MEMBERSHIP_EXPIRED);
    }
}
