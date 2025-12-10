package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureNotFound extends BaseException {
    public LectureNotFound() {
        super(ErrorCode.LECTURE_NOT_FOUND);
    }
}
