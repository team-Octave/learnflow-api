package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class SelfReviewNotAllowedException extends BaseException {
    public SelfReviewNotAllowedException() {
        super(ErrorCode.SELF_REVIEW_NOT_ALLOWED);
    }
}
