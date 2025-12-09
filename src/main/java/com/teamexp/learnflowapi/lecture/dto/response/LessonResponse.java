package com.teamexp.learnflowapi.lecture.dto.response;

import com.teamexp.learnflowapi.lecture.model.Lesson;
import com.teamexp.learnflowapi.lecture.model.LessonType;


public record LessonResponse(
    Long id,
    String lessonTitle,
    LessonType lessonType,
    String lessonTypeDisplayName,
    Integer lessonOrder,
    Boolean isFreePreview
) {
    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(
            lesson.getId(),
            lesson.getLessonTitle(),
            lesson.getLessonType(),
            lesson.getLessonType().getDisplayName(),
            lesson.getLessonOrder(),
            lesson.getIsFreePreview()
        );
    }
}