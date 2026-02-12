package com.teamexp.learnflowapi.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiTaskCompleteResponse(
    boolean success,
    Long taskId,
    Long lessonId,
    String status,
    boolean alreadyCompleted
) {
    public static AiTaskCompleteResponse of(Long taskId, Long lessonId, String status) {
        return new AiTaskCompleteResponse(true, taskId, lessonId, status, false);
    }

    public static AiTaskCompleteResponse alreadyCompleted(Long taskId, Long lessonId) {
        return new AiTaskCompleteResponse(true, taskId, lessonId, "COMPLETED", true);
    }
}
