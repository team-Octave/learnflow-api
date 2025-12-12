package com.teamexp.learnflowapi.content.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuizSaveRequest(
        @NotNull(message = "퀴즈 목록이 비어있습니다.")
        @Size(min = 1, message = "퀴즈는 최소 1개 이상이어야 합니다.")
        @Valid
        List<QuizRequest> quizzes
) {
}
