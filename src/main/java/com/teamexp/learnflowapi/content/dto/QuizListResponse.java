package com.teamexp.learnflowapi.content.dto;

import com.teamexp.learnflowapi.content.model.Quiz;

import java.util.List;

public record QuizListResponse(
        Long lessonId,
        List<QuizResponse> quizzes
) {
    public static QuizListResponse of(Long lessonId, List<Quiz> quizEntities) {
        List<QuizResponse> quizResponses = quizEntities.stream()
                .map(QuizResponse::from)
                .toList();

        return new QuizListResponse(lessonId, quizResponses);
    }
}
