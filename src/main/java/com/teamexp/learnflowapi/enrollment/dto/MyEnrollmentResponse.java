package com.teamexp.learnflowapi.enrollment.dto;

import com.teamexp.learnflowapi.enrollment.model.EnrollmentStatus;
import com.teamexp.learnflowapi.lecture.model.PaymentType;

import java.util.List;
import java.time.Instant;

public record MyEnrollmentResponse(
        Long lectureId,
        Long enrollmentId,
        PaymentType paymentType,
        Long reviewId,
        String lectureThumbnail,
        String lectureTitle,
        EnrollmentStatus enrollmentStatus,
        Integer progress,
        boolean isAccessible,
        Instant enrolledAt,
        Instant updatedAt,
        Integer reviewRating,
        String reviewContent,
        List<Long> completedLessonIds,
        Long lastCompletedLessonChapterId,
        Long firstChapterId,
        Long firstLessonId

) {
}
