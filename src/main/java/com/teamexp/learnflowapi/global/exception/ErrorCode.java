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
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

    // Lecture 도메인 관련
    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 강좌입니다."),
    SELF_REVIEW_NOT_ALLOWED(HttpStatus.CONFLICT, "본인의 강의는 리뷰할 수 없습니다."),
    NOT_INSTRUCTOR(HttpStatus.FORBIDDEN, "해당 강의의 생성자만 답글을 달 수 있습니다."),
    LECTURE_ALREADY_PUBLISHED(HttpStatus.CONFLICT, "이미 공개된 강의입니다."), // to class as LectureAlreadyPublished
    LECTURE_CANNOT_PUBLISH_WITHOUT_CHAPTER(HttpStatus.BAD_REQUEST, "챕터가 없는 강의는 공개할 수 없습니다."), // to class as LectureCannotPublishedWithoutChapter
    LECTURE_CANNOT_PUBLISH_WITHOUT_LESSON(HttpStatus.BAD_REQUEST, "레슨이 없는 강의는 공개할 수 없습니다."), // to class as LectureCannotPublishedWithoutLesson
    LECTURE_LEVEL_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 강의 레벨입니다."), // to class as LectureLevelInvalid
    LECTURE_STATUS_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 강의 상태입니다."), // to class as LectureStatusInvalid
    LESSON_TYPE_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 레슨 타입입니다."), // to class as LessonTypeInvalid
    LESSON_QUIZ_COUNT_INVALID(HttpStatus.BAD_REQUEST, "QUIZ 레슨은 퀴즈 1~10개가 필요하고, VIDEO 레슨에는 퀴즈를 포함할 수 없습니다."),
    LESSON_VIDEO_URL_INVALID(HttpStatus.BAD_REQUEST, "VIDEO 레슨은 videoUrl이 필수이며, QUIZ 레슨은 videoUrl을 가질 수 없습니다."),
    LECTURE_INSTRUCTOR_UNAUTHORIZED(HttpStatus.FORBIDDEN, "해당 강의의 생성자만 수정할 수 있습니다."), // to class as LectureInstructorUnauthorizedException
    CHAPTER_NOT_FOUND_IN_LECTURE(HttpStatus.NOT_FOUND, "해당 강의에 존재하지 않는 챕터입니다."), // to class as ChapterNotFoundException
    LESSON_NOT_FOUND_IN_CHAPTER(HttpStatus.NOT_FOUND, "해당 챕터에 존재하지 않는 레슨입니다."), // to class as LessonNotFoundException
    LECTURE_SORT_TYPE_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 강의 정렬 타입입니다."), // to class as LectureSortTypeInvalid
    LECTURE_DELETE_BLOCKED(HttpStatus.CONFLICT, "게시된 강좌는 삭제할 수 없습니다."), // to class as LectureDeleteBlockedException
    LECTURE_DELETED(HttpStatus.GONE, "이미 삭제된 강의입니다."), // to class as LectureDeletedException
    LECTURE_NOT_DELETED(HttpStatus.BAD_REQUEST, "삭제되지 않은 강의입니다."), // to class as LectureNotDeletedException

    // Content 도메인 관련
    UPLOAD_NOT_EXIST(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다."),
    FILE_NAME_EMPTY(HttpStatus.BAD_REQUEST, "파일명이 비어있습니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "유효하지 않은 파일명입니다. 확장자가 필요합니다."),
    INVALID_VIDEO_EXTENSION(HttpStatus.BAD_REQUEST, "영상 파일은 mp4 확장자만 허용됩니다."),
    THUMBNAIL_UNSUPPORTED_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다. jpg, jpeg만 업로드 가능합니다."),
    THUMBNAIL_UNSUPPORTED_MIME_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 MIME 타입입니다. image/jpeg만 허용됩니다."),
    THUMBNAIL_FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 최대 허용 용량(10MB)을 초과했습니다."),
    LESSON_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 레슨에 등록된 영상이 없습니다."),
    LECTURE_THUMBNAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 강의의 썸네일이 없습니다."),
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 퀴즈를 찾을 수 없습니다."),
    QUIZ_LESSON_MISMATCH(HttpStatus.BAD_REQUEST, "다른 레슨의 퀴즈는 수정할 수 없습니다."),
    MEDIA_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 영상 정보를 찾을 수 없습니다."),
    MEDIA_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST,"이미 업로드된 영상입니다."),
    SIGNED_URL_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "영상 재생 URL 생성 중 오류가 발생했습니다."),
    MEDIA_NOT_READY(HttpStatus.BAD_REQUEST, "영상 업로드가 아직 완료되지 않았습니다."),
    MEDIA_ALREADY_BOUND(HttpStatus.BAD_REQUEST, "이미 다른 레슨에 연결된 미디어입니다."),
    LESSON_ALREADY_BOUND_TO_MEDIA(HttpStatus.BAD_REQUEST,"이미 레슨에 미디어가 있습니다"),

    // Enrollment 도메인 관련
    ENROLLMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 수강신청이 완료된 강좌입니다."),
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "수강 정보를 찾을 수 없습니다."),
    COMPLETED_LESSON_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 완료 처리된 강의입니다."),
    SELF_ENROLLMENT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "강좌를 개설한 사람은 자신의 강좌를 수강할 수 없습니다."),

    // Review 도메인 관련
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 강의에 대한 리뷰를 작성했습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰입니다."),
    NOT_MY_REVIEW(HttpStatus.FORBIDDEN, "본인의 리뷰만 삭제할 수 있습니다."),
    NOT_ENOUGH_PROGRESS(HttpStatus.BAD_REQUEST, "최소 3개의 레슨을 수강 완료해야 리뷰를 작성할 수 있습니다."),

    // Admin 도메인 관련
    APPROVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "승인 요청 정보를 찾을 수 없습니다.");


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

