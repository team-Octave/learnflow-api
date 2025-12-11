package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureAlreadyPublishedException extends BaseException {
    public LectureAlreadyPublishedException() {
        super(ErrorCode.LECTURE_ALREADY_PUBLISHED);
    }
}
