package com.teamexp.learnflowapi.content.excption;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class InvalidFileNameException extends BaseException {
    public InvalidFileNameException() {
        super(ErrorCode.INVALID_FILE_NAME);
    }
}
