package com.teamexp.learnflowapi.global.exception;

public class BaseException extends RuntimeException{

    private final ErrorCode errorCode;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 기본 메시지를 exception 메시지로 사용
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
