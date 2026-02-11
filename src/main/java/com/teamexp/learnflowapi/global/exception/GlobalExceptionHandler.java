package com.teamexp.learnflowapi.global.exception;

import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j // 로깅을 위한 어노테이션 추가
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 우리가 만든 BaseException 처리
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<?>> handleBaseException(BaseException e) {
        String traceId = MDC.get("traceId");
        ErrorCode errorCode = e.getErrorCode();

        // 비즈니스 예외도 추적을 위해 로그를 남깁니다 (Warn 레벨 권장)
        log.warn("[BaseException] traceId={}, code={}, message={}", traceId, errorCode.name(), errorCode.getMessage());

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(BaseResponse.error(errorCode.name(), errorCode.getMessage(), traceId));
    }

    // 2. 예상하지 못한 에러 처리 (가장 중요 ⭐)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(Exception e) {
        String traceId = MDC.get("traceId");

        // e.printStackTrace() 대신 log.error 사용 (Loki로 스택 트레이스 전체 전송)
        log.error("[UnhandledException] traceId={}, message={}", traceId, e.getMessage(), e);

        ErrorCode error = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(error.getStatus())
                .body(BaseResponse.error(error.name(), error.getMessage(), traceId));
    }

    // 3. 로그인 실패 에러 처리
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException e) {
        String traceId = MDC.get("traceId");
        log.warn("[BadCredentialsException] traceId={}, message={}", traceId, e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(BaseResponse.error("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 틀렸습니다.", traceId));
    }

    // 4. @Valid 검증 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String traceId = MDC.get("traceId");

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("[MethodArgumentNotValidException] traceId={}, errors={}", traceId, errors);

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(BaseResponse.error(errorCode.name(), errorCode.getMessage(), errors, traceId));
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
        String traceId = MDC.get("traceId");
        log.warn("[MissingRequestHeaderException] traceId={}, header={}", traceId, e.getHeaderName());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.error("MISSING_HEADER", e.getHeaderName() + " 헤더가 필요합니다.", traceId));
    }

    // 6. 삭제된 유저 처리
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabled(DisabledException e) {
        String traceId = MDC.get("traceId");
        log.warn("[DisabledException] traceId={}", traceId);

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(BaseResponse.error("USER_DISABLED", "없는 유저입니다.", traceId));
    }

    // 7. 쿠키 누락 처리
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<?> handleMissingCookie(MissingRequestCookieException e) {
        String traceId = MDC.get("traceId");
        log.warn("[MissingRequestCookieException] traceId={}, cookie={}", traceId, e.getCookieName());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.error("MISSING_COOKIE", e.getCookieName() + " 쿠키가 필요합니다.", traceId));
    }
}
