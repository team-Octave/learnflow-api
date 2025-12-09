package com.teamexp.learnflowapi.review.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class ReviewAlreadyExistsException extends BaseException {
    public ReviewAlreadyExistsException() {
        super(ErrorCode.REVIEW_ALREADY_EXISTS);
    }
}
