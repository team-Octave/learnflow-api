package com.teamexp.learnflowapi.global.exception;

import com.teamexp.learnflowapi.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 우리가 만든 BaseException 처리
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<?>> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(BaseResponse.error(errorCode.name(), errorCode.getMessage()));
    }

    // 예상하지 못한 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(Exception e) {
        e.printStackTrace(); // TODO : 필요 시 로깅으로 변경

        ErrorCode error = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(error.getStatus())
                .body(BaseResponse.error(error.name(), error.getMessage()));
    }
}

