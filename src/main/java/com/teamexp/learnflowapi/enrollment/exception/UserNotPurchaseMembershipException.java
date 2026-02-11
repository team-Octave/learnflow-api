package com.teamexp.learnflowapi.enrollment.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class UserNotPurchaseMembershipException extends BaseException {
    public UserNotPurchaseMembershipException() {
        super(ErrorCode.MEMBERSHIP_NOT_FOUND_IN_LECTURE);
    }
}
