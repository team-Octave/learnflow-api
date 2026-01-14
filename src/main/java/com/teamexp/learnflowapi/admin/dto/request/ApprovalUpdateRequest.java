package com.teamexp.learnflowapi.admin.dto.request;

import com.teamexp.learnflowapi.admin.model.ApprovalRejectType;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ApprovalUpdateRequest(
    @NotNull
    ApprovalStatus status,
    List<ApprovalRejectType> rejectCategories,
    String reason
) {
}
