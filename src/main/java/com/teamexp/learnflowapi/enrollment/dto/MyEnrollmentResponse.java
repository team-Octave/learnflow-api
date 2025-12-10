package com.teamexp.learnflowapi.enrollment.dto;

import com.teamexp.learnflowapi.enrollment.model.EnrollmentStatus;

import java.time.Instant;

public record MyEnrollmentResponse(
        Long lectureId,
        Long enrollmentId,
        Long reviewId,
        String lectureThumbnail,
        String lectureTitle,
        EnrollmentStatus enrollmentStatus,
        Integer progress,
        Instant enrolledAt,
        Instant updatedAt,
        Integer reviewRating,
        String reviewContent
) {
}
