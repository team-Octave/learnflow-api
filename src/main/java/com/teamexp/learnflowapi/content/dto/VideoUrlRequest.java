package com.teamexp.learnflowapi.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VideoUrlRequest(
        @NotBlank(message = "Video URL은 필수입니다.")
        String videoUrl,
        @NotNull(message = "Lesson ID는 필수입니다.")
        Long lessonId
) {
}
