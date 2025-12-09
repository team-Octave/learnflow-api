package com.teamexp.learnflowapi.auth.exception;

import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;

public class RefreshTokenInavlidException extends BaseException {
    public RefreshTokenInavlidException() {
        super(ErrorCode.REFRESH_TOKEN_INVALID);
    }
}
