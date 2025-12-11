package com.teamexp.learnflowapi.lecture.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class LectureCannotPublishedWithoutChapterException extends BaseException {
    public LectureCannotPublishedWithoutChapterException() {
        super(ErrorCode.LECTURE_CANNOT_PUBLISH_WITHOUT_CHAPTER);
    }
    
}
