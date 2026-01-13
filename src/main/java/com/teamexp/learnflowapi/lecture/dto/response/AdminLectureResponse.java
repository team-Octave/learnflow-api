package com.teamexp.learnflowapi.lecture.dto.response;

import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatistic;

import java.time.Instant;

public record AdminLectureResponse(
    Long id,
    String title,
    String instructorId,
    boolean deleteFlag,
    Instant deletedAt,
    Instant createdAt,
    Instant updatedAt,
    Double ratingAverage,
    Long enrollmentCount
) {
    public static AdminLectureResponse from(Lecture lecture) {
        LectureStatistic stat = lecture.getStatistic();
        Double ratingAvg = stat != null && stat.getRatingAverage() != null ? stat.getRatingAverage() : 0.0;
        Long enrollmentCnt = stat != null && stat.getEnrollmentCount() != null ? stat.getEnrollmentCount() : 0L;

        return new AdminLectureResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getInstructorId(),
            lecture.isDeleteFlag(),
            lecture.getDeletedAt(),
            lecture.getCreatedAt(),
            lecture.getUpdatedAt(),
            ratingAvg,
            enrollmentCnt
        );
    }
}


