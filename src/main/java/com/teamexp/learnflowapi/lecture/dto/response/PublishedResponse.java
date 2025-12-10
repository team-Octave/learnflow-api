package com.teamexp.learnflowapi.lecture.dto.response;

import com.teamexp.learnflowapi.lecture.model.LectureStatus;

public record PublishedResponse(
    Long lectureId,
    LectureStatus status
) {
    public static PublishedResponse from(Long lectureId, LectureStatus status) {
        return new PublishedResponse(lectureId, status);
    }
}
