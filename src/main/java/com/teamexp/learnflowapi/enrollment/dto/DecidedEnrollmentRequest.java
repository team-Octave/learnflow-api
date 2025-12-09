package com.teamexp.learnflowapi.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DecidedEnrollmentRequest(
        @NotNull @Positive Long enrollmentId
) {
}
