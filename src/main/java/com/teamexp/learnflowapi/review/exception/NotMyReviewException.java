package com.teamexp.learnflowapi.review.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class NotMyReviewException extends BaseException {
    public NotMyReviewException() {
        super(ErrorCode.NOT_MY_REVIEW);
    }
}
