package com.teamexp.learnflowapi.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiTaskHeartbeatRequest(
    @NotBlank String workerId,
    @Min(0) @Max(100) int progress,
    String currentStep
) {}
