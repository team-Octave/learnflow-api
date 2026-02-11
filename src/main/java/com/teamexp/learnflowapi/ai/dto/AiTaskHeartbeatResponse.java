package com.teamexp.learnflowapi.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiTaskHeartbeatResponse(
    boolean success,
    boolean shouldContinue,
    String reason
) {
    public static AiTaskHeartbeatResponse continueProcessing() {
        return new AiTaskHeartbeatResponse(true, true, null);
    }

    public static AiTaskHeartbeatResponse stopProcessing(String reason) {
        return new AiTaskHeartbeatResponse(true, false, reason);
    }
}
