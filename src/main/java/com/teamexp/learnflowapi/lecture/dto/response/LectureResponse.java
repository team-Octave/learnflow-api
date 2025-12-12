package com.teamexp.learnflowapi.lecture.dto.response;

import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureLevel;
import com.teamexp.learnflowapi.lecture.model.LectureStatistic;

import java.time.Instant;

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
    String instructorDisplayName,
    int totalChapterCount,
    int totalLessonCount,
    Instant createdAt,
    List<ChapterResponse> chapters,
    Double ratingAverage,
    Long enrollmentCount,
    String thumbnailUrl
) {
    // Full detail including chapters and lessons info  with counts
    public static LectureResponse from(Lecture lecture, String thumbnailUrl, String instructorDisplayName) {
        return new LectureResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getDescription(),
            lecture.getCategoryId(),
            lecture.getLevel(),
            lecture.getLevel().getDisplayName(),
            lecture.getStatus().getDisplayName(),
            lecture.getInstructorId(),
            instructorDisplayName,
            lecture.getChapters().size(),
            lecture.getTotalLessonCount(),
            lecture.getCreatedAt(),
            lecture.getChapters().stream()
                .map(ChapterResponse::from)
                .collect(Collectors.toList()),
            null,
            null,
            thumbnailUrl
        );
    }

    // Full detail including chapters and lessons info  without counts
    public static LectureResponse simpleFrom(Lecture lecture, String instructorDisplayName) {
        return new LectureResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getDescription(),
            lecture.getCategoryId(),
            lecture.getLevel(),
            lecture.getLevel().getDisplayName(),
            lecture.getStatus().getDisplayName(),
            lecture.getInstructorId(),
            instructorDisplayName,
            0,
            0,
            lecture.getCreatedAt(),
            null,
            null,
            null,
            null
        );
    }

    // Simple response with statistics for list view
    public static LectureResponse simpleFromWithStats(Lecture lecture, LectureStatistic statistic, String thumbnailUrl,String instructorDisplayName) {
        Double ratingAvg = 0.0;
        Long enrollmentCnt = 0L;
        
        if (statistic != null) {
            ratingAvg = statistic.getRatingAverage() != null ? statistic.getRatingAverage() : 0.0;
            enrollmentCnt = statistic.getEnrollmentCount() != null ? statistic.getEnrollmentCount() : 0L;
        }
        
        return new LectureResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getDescription(),
            lecture.getCategoryId(),
            lecture.getLevel(),
            lecture.getLevel().getDisplayName(),
            lecture.getStatus().getDisplayName(),
            lecture.getInstructorId(),
            instructorDisplayName,
            0,
            0,
            lecture.getCreatedAt(),
            null,
            ratingAvg,
            enrollmentCnt,
            thumbnailUrl
        );
    }
}
