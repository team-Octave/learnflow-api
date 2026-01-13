package com.teamexp.learnflowapi.admin.dto;

import java.util.List;

public record ApprovalsResponse(
    int total,
    int page,
    int size,
    List<ApprovalDto> approvals
) {
}
