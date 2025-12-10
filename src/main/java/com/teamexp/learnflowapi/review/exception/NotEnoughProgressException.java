package com.teamexp.learnflowapi.review.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class NotEnoughProgressException extends BaseException {
    public NotEnoughProgressException() {
        super(ErrorCode.NOT_ENOUGH_PROGRESS);
    }
}
