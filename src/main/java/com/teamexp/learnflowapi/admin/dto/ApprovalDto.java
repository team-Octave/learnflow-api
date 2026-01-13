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

}
