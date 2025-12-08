package com.teamexp.learnflowapi.enrollment.dto;

import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.model.EnrollmentStatus;

import java.time.Instant;

public record EnrollmentResponse(
        Long userId,
        Long lectureId,
        EnrollmentStatus enrollmentStatus,
        Integer progress,
        Instant enrolledAt,
        Instant updatedAt
) {
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getUserId(),
                enrollment.getLectureId(),
                enrollment.getStatus(),
                enrollment.getProgress(),
                enrollment.getEnrolledAt(),
                enrollment.getUpdatedAt());
    }
}
