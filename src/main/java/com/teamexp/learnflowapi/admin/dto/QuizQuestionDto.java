package com.teamexp.learnflowapi.admin.dto;

import com.teamexp.learnflowapi.lecture.model.Quiz;

public record QuizQuestionDto(
    Long id,
    Integer orderIndex,
    String question,
    Boolean correct
) {

    public static QuizQuestionDto from(Quiz quiz) {
        return new QuizQuestionDto(
            quiz.getId(),
            quiz.getOrderIndex(),
            quiz.getQuestion(),
            quiz.getCorrect()
        );
    }
}

