package com.teamexp.learnflowapi.admin.dto;

import com.teamexp.learnflowapi.lecture.model.Chapter;
import java.util.List;

public record ChapterDto(
    Long chapterId,
    String chapterTitle,
    Integer order,
    List<LessonDto> lessons
) {
    public static ChapterDto from(Chapter chapter) {
        return new ChapterDto(
            chapter.getId(),
            chapter.getChapterTitle(),
            chapter.getChapterOrder(),
            chapter.getLessons().stream()
                .map(LessonDto::from)
                .toList()
        );
    }
}
