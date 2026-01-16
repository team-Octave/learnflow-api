package com.teamexp.learnflowapi.lecture.dto.request;

import jakarta.validation.constraints.Size;

public record LectureBaseUpdateRequest(
    @Size(max = 100, message = "강의 제목은 100자를 초과할 수 없습니다.")
    String title,
    String description,
    String thumbnailUrl
) {
    
}
