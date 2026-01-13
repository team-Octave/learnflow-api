package com.teamexp.learnflowapi.lecture.dto.response;

import com.teamexp.learnflowapi.lecture.model.Quiz;

public record QuizResponse(
        Long id,
        Integer orderIndex,
        String question,
        Boolean correct
) {
    public static QuizResponse from(Quiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getOrderIndex(),
                quiz.getQuestion(),
                quiz.getCorrect()
        );
    }
}

