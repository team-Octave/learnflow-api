package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureNotFoundException extends BaseException {
    public LectureNotFoundException() {
        super(ErrorCode.LECTURE_NOT_FOUND);
    }
}
