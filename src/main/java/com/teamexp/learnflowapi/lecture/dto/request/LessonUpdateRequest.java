package com.teamexp.learnflowapi.lecture.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * Lesson 부분 수정용 DTO.
 *
 * <p>PATCH semantics를 고려하여 필드는 nullable로 두고, Service에서 null 여부에 따라 적용한다.
 */
public record LessonUpdateRequest(
    @Size(max = 200, message = "레슨 제목은 200자를 초과할 수 없습니다.")
    String lessonTitle,
    Boolean isFreePreview,
    String videoUrl,
    @Valid
    QuizUpdateListRequest quizQuestions
) {

}


