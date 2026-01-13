package com.teamexp.learnflowapi.admin.controller;

import com.teamexp.learnflowapi.admin.controller.dto.ApprovalsResponse;
import com.teamexp.learnflowapi.admin.service.ApprovalService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable
        ) {
        ApprovalsResponse response = approvalService.getApprovals(pageable);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BaseResponse.ok(response));
    }
}
