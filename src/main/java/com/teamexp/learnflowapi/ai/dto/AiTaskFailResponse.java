package com.teamexp.learnflowapi.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiTaskFailResponse(
    boolean success,
    Long taskId,
    String newStatus,
    int retryCount,
    Instant nextAttemptAt,
    boolean alreadyProcessed
) {
    public static AiTaskFailResponse of(Long taskId, String newStatus, int retryCount, Instant nextAttemptAt) {
        return new AiTaskFailResponse(true, taskId, newStatus, retryCount, nextAttemptAt, false);
    }

    public static AiTaskFailResponse alreadyProcessed(Long taskId, String status, int retryCount) {
        return new AiTaskFailResponse(true, taskId, status, retryCount, null, true);
    }
}
