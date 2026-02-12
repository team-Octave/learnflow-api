package com.teamexp.learnflowapi.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiTaskCompleteRequest(
    @NotBlank String workerId,
    int durationSeconds,
    String durationFormatted,
    @NotBlank String transcript,
    @NotNull Map<String, Object> fullAnalysis,
    @NotBlank String summary,
    String modelVersion,
    int processingTimeSeconds
) {}
