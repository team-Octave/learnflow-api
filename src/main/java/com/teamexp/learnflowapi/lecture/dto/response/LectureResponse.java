package com.teamexp.learnflowapi.lecture.dto.response;

import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureLevel;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record LectureResponse(
    Long id,
    String title,
    String description,
    Integer categoryId,
    LectureLevel level,
    String levelDisplayName,
    String statusDisplayName,
    String instructorId,
    int totalChapterCount,
    int totalLessonCount,
    Instant createdAt,
    List<ChapterResponse> chapters
) {
    // Full detail including chapters and lessons info  with counts
    public static LectureResponse from(Lecture lecture) {
        return new LectureResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getDescription(),
            lecture.getCategoryId(),
            lecture.getLevel(),
            lecture.getLevel().getDisplayName(),
            lecture.getStatus().getDisplayName(),
            lecture.getInstructorId(),
            lecture.getChapters().size(),
            lecture.getTotalLessonCount(),
            lecture.getCreatedAt(),
            lecture.getChapters().stream()
                .map(ChapterResponse::from)
                .collect(Collectors.toList())
        );
    }

    // Full detail including chapters and lessons info  without counts
    public static LectureResponse simpleFrom(Lecture lecture) {
        return new LectureResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getDescription(),
            lecture.getCategoryId(),
            lecture.getLevel(),
            lecture.getLevel().getDisplayName(),
            lecture.getStatus().getDisplayName(),
            lecture.getInstructorId(),
            0,
            0,
            lecture.getCreatedAt(),
            null
        );
    }
}
