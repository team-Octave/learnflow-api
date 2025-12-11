package com.teamexp.learnflowapi.content.dto;

import com.teamexp.learnflowapi.content.model.Quiz;
import java.util.List;

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

