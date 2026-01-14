package com.teamexp.learnflowapi.admin.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class ApprovalNotFoundException extends BaseException {
    public ApprovalNotFoundException() {
        super(ErrorCode.APPROVAL_NOT_FOUND);
    }
}
