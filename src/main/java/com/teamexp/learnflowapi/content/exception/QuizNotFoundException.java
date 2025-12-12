package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class QuizNotFoundException extends BaseException {
    public QuizNotFoundException() {
        super(ErrorCode.QUIZ_NOT_FOUND);
    }
}
