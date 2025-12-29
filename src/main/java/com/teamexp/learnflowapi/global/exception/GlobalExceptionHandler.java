package com.teamexp.learnflowapi.global.exception;

import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
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

    // 로그인 실패 시 발생하는 에러 처리
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.error("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 틀렸습니다."));
    }

    // @Valid @RequestBody DTO 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {

        // 필드별 에러 메시지 모으기
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(BaseResponse.error(errorCode.name(), errorCode.getMessage(), errors));
    }

//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<BaseResponse<?>> handleConstraintViolation(ConstraintViolationException e) {
//
//        Map<String, String> errors = new HashMap<>();
//        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
//            // propertyPath: "createUser.request.email" 이런 식으로 올 수 있음
//            String field = violation.getPropertyPath().toString();
//            errors.put(field, violation.getMessage());
//        }
//
//        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
//
//        return ResponseEntity
//            .status(errorCode.getStatus())
//            .body(BaseResponse.error(errorCode.name(), errorCode.getMessage(), errors));
//    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<?> handleMissingHeader(MissingRequestHeaderException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.error("MISSING_HEADER", e.getHeaderName() + " 헤더가 필요합니다."));
    }



    /*
    * 유저가 논리적으로 삭제 되었을 때, 발생하는 에러 처리
    * */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabled(DisabledException e) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(BaseResponse.error("USER_DISABLED", "없는 유저입니다."));
    }



}

