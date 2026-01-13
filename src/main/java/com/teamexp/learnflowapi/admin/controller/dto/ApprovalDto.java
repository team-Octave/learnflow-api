package com.teamexp.learnflowapi.admin.controller.dto;

import java.time.Instant;

public record ApprovalDto(
    long approvalId,
    long lectureId,
    String thumbnailUrl,
    String lectureTitle,
    String nickname,
    Instant requestDate,
    String lectureStatus
) {

}
