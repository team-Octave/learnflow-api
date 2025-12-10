package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class SelfReviewNowAllowedException extends BaseException {
    public SelfReviewNowAllowedException() {
        super(ErrorCode.SELF_REVIEW_NOT_ALLOWED);
    }
}
