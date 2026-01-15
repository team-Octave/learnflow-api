package com.teamexp.learnflowapi.admin.dto;

import com.teamexp.learnflowapi.lecture.model.Lesson;
import java.util.List;

public record LessonReadDetailResponse(
    Long id,
    String lessonTitle,
    String lessonTypeDisplayName,
    Integer lessonOrder,
    String videoUrl,
    List<QuizQuestionDto> quizQuestions
) {

    public static LessonReadDetailResponse from(Lesson lesson) {
        return new LessonReadDetailResponse(
            lesson.getId(),
            lesson.getLessonTitle(),
            lesson.getLessonType().getDisplayName(),
            lesson.getLessonOrder(),
            lesson.getVideoUrl(),
            lesson.getQuizzes().stream()
                .map(QuizQuestionDto::from)
                .toList()
        );
    }
}
