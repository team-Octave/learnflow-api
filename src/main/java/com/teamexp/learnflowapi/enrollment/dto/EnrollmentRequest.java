package com.teamexp.learnflowapi.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EnrollmentRequest(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long lectureId
) {
}
