package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class ChapterNotFoundException extends BaseException {
    public ChapterNotFoundException() {
        super(ErrorCode.CHAPTER_NOT_FOUND_IN_LECTURE);
    }
}
