package com.teamexp.learnflowapi.lecture.dto.response;

import com.teamexp.learnflowapi.lecture.model.Lesson;
import com.teamexp.learnflowapi.lecture.model.LessonType;

import java.util.List;


public record LessonResponse(
    Long id,
    String lessonTitle,
    String lessonTypeDisplayName,
    Integer lessonOrder,
    Boolean isFreePreview,
    String videoUrl,
    List<QuizQuestionResponse> quizQuestions

) {
    public record QuizQuestionResponse(
        Long id,
        String question,
        Integer questionOrder,
        Boolean correct
    ) {
    }

    public static LessonResponse withoutQuiz(
        Long lessonId,
        String lessonTitle,
        String lessonTypeDisplayName,
        Integer order,
        Boolean isFreePreview,
        String videoUrl
    ) {
        return new LessonResponse(
            lessonId,
            lessonTitle,
            lessonTypeDisplayName,
            order,
            isFreePreview,
            videoUrl,
            null
        );
    }

    public static LessonResponse withoutVideo(
        Long lessonId,
        String lessonTitle,
        String lessonTypeDisplayName,
        Integer order,
        Boolean isFreePreview,
        List<LessonResponse.QuizQuestionResponse> quizQuestions
    ) {
        return new LessonResponse(
            lessonId,
            lessonTitle,
            lessonTypeDisplayName,
            order,
            isFreePreview,
            null,
            quizQuestions
        );
    }


}