package com.teamexp.learnflowapi.lecture.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuizSaveRequest(
        @NotNull(message = "퀴즈 목록이 비어있습니다.")
        @Size(min = 1,max = 10, message = "퀴즈는 최소 1개, 최대 10개까지 가능합니다.")
        @Valid
        List<QuizRequest> quizQuestions
) {
}
