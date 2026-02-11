package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class MembershipNotFoundException extends BaseException {
    public MembershipNotFoundException() {
        super(ErrorCode.MEMBERSHIP_NOT_FOUND_IN_LECTURE);
    }
}
