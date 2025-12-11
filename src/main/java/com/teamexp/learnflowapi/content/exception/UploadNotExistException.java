package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class UploadNotExistException extends BaseException {
    public UploadNotExistException() {
        super(ErrorCode.UPLOAD_NOT_EXIST);
    }
}
