package com.teamexp.learnflowapi.enrollment.dto;

import java.util.List;

public record SelectEnrollmentResponse(
        Long enrollmentId,
        Long lectureId,
        Integer progress,
        List<Long> completedLessonIds,
        Long lastCompletedLessonChapterId

) {
}
