package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureSortTypeInvalidException extends BaseException {
    public LectureSortTypeInvalidException() {
        super(ErrorCode.LECTURE_SORT_TYPE_INVALID);
    }
}
