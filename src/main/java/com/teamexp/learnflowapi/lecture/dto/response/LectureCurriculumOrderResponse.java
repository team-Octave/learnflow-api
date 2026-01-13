package com.teamexp.learnflowapi.lecture.dto.response;

import java.util.List;

public record LectureCurriculumOrderResponse(
    Long lectureId,
    List<ChapterResponse> chapters
) {
    public record ChapterResponse(
        Long chapterId,
        Integer order,
        List<LessonResponse> lessons
    ) {
    }

    public record LessonResponse(
        Long lessonId,
        Integer order
    ) {}

    public static LectureCurriculumOrderResponse from(Long lectureId, List<ChapterResponse> chapters) {
        return new LectureCurriculumOrderResponse(lectureId, chapters);
    }
}
