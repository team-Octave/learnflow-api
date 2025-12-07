package com.teamexp.learnflowapi.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GetEnrollmentRequest(
//      TODO Long -> String
        @NotNull @Positive Long userId
) {
}
