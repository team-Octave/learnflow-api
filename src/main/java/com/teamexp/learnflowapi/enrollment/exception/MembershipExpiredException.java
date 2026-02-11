package com.teamexp.learnflowapi.enrollment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class MembershipExpiredException extends BaseException {
    public MembershipExpiredException() {
        super(ErrorCode.MEMBERSHIP_EXPIRED);
    }
}
