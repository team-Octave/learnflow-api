package com.teamexp.learnflowapi.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCompletedLessonRequest(
        @NotNull @Positive Long enrollmentId,
        @NotNull @Positive Long lessonId
) {
}
