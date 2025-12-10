package com.teamexp.learnflowapi.content.dto;

public record VideoUrlRequest(
        String videoUrl,
        Long lessonId
) {
}
