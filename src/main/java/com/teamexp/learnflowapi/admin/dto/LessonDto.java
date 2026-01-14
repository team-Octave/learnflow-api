package com.teamexp.learnflowapi.admin.dto;

import com.teamexp.learnflowapi.lecture.model.Lesson;

public record LessonDto(
    Integer order,
    Long lessonId
) {

    public static LessonDto from(Lesson lesson) {
        return new LessonDto(
            lesson.getLessonOrder(),
            lesson.getId()
        );
    }
}
