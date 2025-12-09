package com.teamexp.learnflowapi.lecture.dto.request;

import com.teamexp.learnflowapi.lecture.model.LessonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LessonCreateRequest(
    @NotBlank(message = "레슨 제목은 필수입니다.")
    @Size(max = 200, message = "레슨 제목은 200자를 초과할 수 없습니다.")
    String lessonTitle,

    @NotNull(message = "레슨 타입은 필수입니다.")
    LessonType lessonType,

    Boolean isFreePreview
) {
}
