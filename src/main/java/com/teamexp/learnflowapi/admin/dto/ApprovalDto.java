package com.teamexp.learnflowapi.admin.dto;

import java.time.Instant;

public record ApprovalDto(
    long lectureId,
    String thumbnailUrl,
    String lectureTitle,
    String nickname,
    Instant requestDate,
    String lectureStatus
) {
    // 매직 스트링 상수화
    private static final String UNKNOWN_INSTRUCTOR = "알 수 없음";

    // Compact Constructor: 생성자 내부에서 검증 및 기본값 할당
    public ApprovalDto {
        if (nickname == null || nickname.isBlank()) {
            nickname = UNKNOWN_INSTRUCTOR;
        }
    }
}
