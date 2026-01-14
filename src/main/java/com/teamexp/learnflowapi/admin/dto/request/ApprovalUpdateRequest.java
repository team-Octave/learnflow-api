package com.teamexp.learnflowapi.admin.dto.request;

import com.teamexp.learnflowapi.admin.model.ApprovalRejectType;
import java.util.List;

public record ApprovalUpdateRequest(
    ApprovalStatus status,
    List<ApprovalRejectType> rejectCategories,
    String reason
) {
}
