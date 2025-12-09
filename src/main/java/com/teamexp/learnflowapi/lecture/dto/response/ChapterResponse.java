package com.teamexp.learnflowapi.lecture.dto.response;

import com.teamexp.learnflowapi.lecture.model.Chapter;


import java.util.List;
import java.util.stream.Collectors;

public record ChapterResponse(
    Long id,
    String chapterTitle,
    Integer chapterOrder,
    int lessonCount,
    List<LessonResponse> lessons
) {
    public static ChapterResponse from(Chapter chapter) {
        return new ChapterResponse(
            chapter.getId(),
            chapter.getChapterTitle(),
            chapter.getChapterOrder(),
            chapter.getLessons().size(),
            chapter.getLessons().stream()
                .map(LessonResponse::from)
                .collect(Collectors.toList())
        );
    }
}