package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class FileNameEmptyException extends BaseException {
    public FileNameEmptyException() {
        super(ErrorCode.FILE_NAME_EMPTY);
    }
}
