package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureStatusInvalidException extends BaseException {
    public LectureStatusInvalidException() {
        super(ErrorCode.LECTURE_STATUS_INVALID);
    }
}
