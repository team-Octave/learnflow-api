package com.teamexp.learnflowapi.lecture.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChapterCreateRequest(
    @NotBlank(message = "챕터 제목은 필수 입력 값입니다.")
    @Size(max = 100, message = "챕터 제목은 100자 이내로 작성해주세요.")
    String chapterTitle
) {
}
