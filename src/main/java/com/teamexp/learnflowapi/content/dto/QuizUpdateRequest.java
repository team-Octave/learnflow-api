package com.teamexp.learnflowapi.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizUpdateRequest(
        Long id,
        @NotNull
        Integer orderIndex,
        @NotBlank(message = "문제 내용은 비어있을 수 없습니다.")
        String question,
        @NotNull(message = "정답 여부는 필수 입니다.")
        Boolean correct
) {
}
