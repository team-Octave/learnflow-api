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
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // Lecture 도메인 관련
    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 강좌입니다."),
    SELF_REVIEW_NOT_ALLOWED(HttpStatus.CONFLICT, "본인의 강의는 리뷰할 수 없습니다."),
    NOT_INSTRUCTOR(HttpStatus.FORBIDDEN, "해당 강의의 생성자만 답글을 달 수 있습니다."),

    // Content 도메인 관련
    UPLOAD_NOT_EXIST(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "유효하지 않은 파일명입니다. 확장자가 필요합니다."),
    THUMBNAIL_UNSUPPORTED_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다. jpg, jpeg만 업로드 가능합니다."),
    THUMBNAIL_UNSUPPORTED_MIME_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 MIME 타입입니다. image/jpg, image/jpeg만 허용됩니다."),
    THUMBNAIL_FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 최대 허용 용량(10MB)을 초과했습니다."),


    // Enrollment 도메인 관련
    ENROLLMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 수강신청이 완료된 강좌입니다."),
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "수강 정보를 찾을 수 없습니다."),
    COMPLETED_LESSON_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 완료 처리된 강의입니다."),

    // Review 도메인 관련
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 강의에 대한 리뷰를 작성했습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰입니다."),
    NOT_MY_REVIEW(HttpStatus.FORBIDDEN, "본인의 리뷰만 삭제할 수 있습니다."),
    NOT_ENOUGH_PROGRESS(HttpStatus.BAD_REQUEST, "최소 3개의 레슨을 수강 완료해야 리뷰를 작성할 수 있습니다.");

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

