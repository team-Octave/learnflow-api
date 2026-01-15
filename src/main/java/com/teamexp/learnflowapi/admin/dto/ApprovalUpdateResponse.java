package com.teamexp.learnflowapi.admin.dto;

import java.time.Instant;

public record ApprovalUpdateResponse(
    Long lectureId,
    String status,
    Instant approvedAt
) {
}
