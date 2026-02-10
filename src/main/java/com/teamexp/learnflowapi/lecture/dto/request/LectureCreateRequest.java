package com.teamexp.learnflowapi.lecture.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Deprecated
public record LectureCreateRequest(
    @NotBlank(message = "강의 제목은 필수 입력 값입니다.")
    @Size(message = "강의 제목은 100자 이내로 작성해주세요.", max = 100)
    String title,

    String description,

    @NotNull(message = "카테고리는 필수 입력 값입니다.")
    Integer categoryId,

    @NotNull(message = "난이도는 필수 입력 값입니다.")
    String level,

    // nullable in V1, but make it mandatory in V2
    String thumbnailUrl,

    @NotNull(message = "강의 유형은 필수 입력 값입니다.")
    String paymentType
) {
}
