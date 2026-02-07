package com.teamexp.learnflowapi.admin.controller;

import com.teamexp.learnflowapi.admin.dto.ApprovalDetailResponse;
import com.teamexp.learnflowapi.admin.dto.ApprovalUpdateResponse;
import com.teamexp.learnflowapi.admin.dto.ApprovalsResponse;
import com.teamexp.learnflowapi.admin.dto.request.ApprovalUpdateRequest;
import com.teamexp.learnflowapi.admin.service.ApprovalService;
import com.teamexp.learnflowapi.global.aop.LogTrace;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    public ResponseEntity<BaseResponse<ApprovalsResponse>> getApprovals(
        // TODO : sort 조건을 oldest로 해야하는데 이거 어떻게 바꾸는지 잘 모르겠음.
        @PageableDefault(size = 5, sort = "updatedAt", direction = Sort.Direction.ASC)Pageable pageable
        ) {
        ApprovalsResponse response = approvalService.getApprovals(pageable);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BaseResponse.ok(response));
    }

    // TODO : approvalId를 추후에 변경해야함.
    @GetMapping("/{lectureId}")
    public ResponseEntity<BaseResponse<ApprovalDetailResponse>> getApproval(
        @PathVariable Long lectureId
    ) {
        ApprovalDetailResponse response = approvalService.getApproval(lectureId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BaseResponse.ok(response));
    }

    // TODO : approvalId로 추후에 변경해야함.
    @PatchMapping("/{lectureId}")
    public ResponseEntity<BaseResponse<ApprovalUpdateResponse>> updateApprovalStatus(
        @PathVariable Long lectureId,
        @Valid @RequestBody ApprovalUpdateRequest request
    ) {
        ApprovalUpdateResponse response = approvalService.updateApproval(lectureId, request);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BaseResponse.ok(response));
    }
}
