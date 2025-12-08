package com.teamexp.learnflowapi.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 공통 (Global)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력 값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // User 도메인 관련
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    // Lecture 도메인 관련

    // Content 도메인 관련

    // Enrollment 도메인 관련
    ENROLLMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 수강신청이 완료된 강좌입니다."),
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "수강 정보를 찾을 수 없습니다."),

    // Review 도메인 관련

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}

