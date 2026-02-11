package com.teamexp.learnflowapi.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiTaskFailRequest(
    @NotBlank String workerId,
    @NotBlank String errorCode,
    String errorMessage,
    boolean isRetryable
) {}
