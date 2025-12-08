package com.teamexp.learnflowapi.enrollment.dto;

public record CreateCompletedLessonRequest(
        Long enrollmentId,
        Long lessonId
) {
}
