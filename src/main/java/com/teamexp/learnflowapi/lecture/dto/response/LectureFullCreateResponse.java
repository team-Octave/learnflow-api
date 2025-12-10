package com.teamexp.learnflowapi.lecture.dto.response;

import java.util.List;

public record LectureFullCreateResponse(
    Long lectureId,
    String title,
    String description,
    Integer categoryId,
    String level, // Display name of LectureLevel
    List<ChapterResponse> chapters
) {
    public record ChapterResponse(
        Long chapterId,
        String chapterTitle,
        Integer order,
        List<LessonResponse> lessons
    ) {
    }

    public record LessonResponse(
        Long lessonId,
        String lessonTitle,
        Integer order,
        String lessonType, // Display name of LessonType
        Boolean isFreePreview
    ) {
    }
}
