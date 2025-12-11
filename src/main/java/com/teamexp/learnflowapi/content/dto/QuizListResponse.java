package com.teamexp.learnflowapi.content.dto;

import com.teamexp.learnflowapi.content.model.Quiz;

import java.util.List;

public record QuizListResponse(
        List<QuizResponse> quizzes
) {
    public static QuizListResponse of(List<Quiz> quizEntities) {
        List<QuizResponse> quizResponses = quizEntities.stream()
                .map(QuizResponse::from)
                .toList();

        return new QuizListResponse(quizResponses);
    }
}
