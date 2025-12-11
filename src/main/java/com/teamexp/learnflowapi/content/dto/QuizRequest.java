package com.teamexp.learnflowapi.content.dto;

public record QuizRequest(
        Integer orderIndex,
        String question,
        Boolean correct
) {
}
