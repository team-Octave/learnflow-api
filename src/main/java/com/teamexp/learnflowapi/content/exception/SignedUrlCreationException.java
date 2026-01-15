package com.teamexp.learnflowapi.content.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class SignedUrlCreationException extends BaseException {
    public SignedUrlCreationException() {
        super(ErrorCode.SIGNED_URL_CREATION_FAILED);
    }
}
