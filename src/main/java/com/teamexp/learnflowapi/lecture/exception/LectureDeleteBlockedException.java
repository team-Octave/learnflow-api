package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureDeleteBlockedException extends BaseException {
    public LectureDeleteBlockedException() {
        super(ErrorCode.LECTURE_DELETE_BLOCKED);
    }
}
