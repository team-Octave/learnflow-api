package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class QuizLessonMismatchException extends BaseException {
    public QuizLessonMismatchException() {
        super(ErrorCode.QUIZ_LESSON_MISMATCH);
    }
}
