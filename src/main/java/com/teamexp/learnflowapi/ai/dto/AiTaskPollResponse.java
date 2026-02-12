package com.teamexp.learnflowapi.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiTaskPollResponse(
    Long taskId,
    Long lessonId,
    String taskType,
    String videoUrl,
    String signedUrl,
    Instant signedUrlExpiresAt,
    String title,
    int retryCount,
    Instant createdAt,
    String message
) {
    public static AiTaskPollResponse empty() {
        return new AiTaskPollResponse(null, null, null, null, null, null, null, 0, null, "No pending tasks");
    }

    public static AiTaskPollResponse of(
        Long taskId,
        Long lessonId,
        String signedUrl,
        Instant signedUrlExpiresAt,
        String title,
        int retryCount,
        Instant createdAt
    ) {
        return new AiTaskPollResponse(
            taskId,
            lessonId,
            "LESSON_SUMMARY",
            null, // videoUrl - original GCS path (not exposed)
            signedUrl,
            signedUrlExpiresAt,
            title,
            retryCount,
            createdAt,
            null
        );
    }
}
