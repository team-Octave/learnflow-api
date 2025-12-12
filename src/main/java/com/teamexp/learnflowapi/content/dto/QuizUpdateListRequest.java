package com.teamexp.learnflowapi.content.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuizUpdateListRequest(
        @NotNull
        @Size(min = 1)
        @Valid List<QuizUpdateRequest> quizzes
) {
}
