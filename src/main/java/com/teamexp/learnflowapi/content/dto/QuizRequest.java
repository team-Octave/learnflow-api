package com.teamexp.learnflowapi.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizRequest(
        @NotNull
        Integer orderIndex,
        @NotBlank(message = "문제 내용은 비어있을 수 없습니다.")
        String question,
        @NotNull
        Boolean correct
) {
}
