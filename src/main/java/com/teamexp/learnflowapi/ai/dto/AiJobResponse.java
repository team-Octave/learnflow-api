package com.teamexp.learnflowapi.ai.dto;

public record AiJobResponse(
    Long taskId,
    String videoUrl, // GCS Signed URL
    String title     // AI에게 제공할 컨텍스트(강의 제목)
) {}
