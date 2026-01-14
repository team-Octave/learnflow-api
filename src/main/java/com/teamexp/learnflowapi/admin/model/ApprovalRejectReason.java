package com.teamexp.learnflowapi.admin.model;

import jakarta.persistence.Column;

public class ApprovalRejectReason {

    @Column(name = "content_quality_low")
    private Boolean contentQualityLow;

    @Column(name = "lecture_info_mismatch")
    private Boolean lectureInfoMismatch;

    @Column(name = "media_quality_issue")
    private Boolean mediaQualityIssue;

    @Column(name = "policy_violation")
    private Boolean policyViolations;

    @Column(name = "other")
    private Boolean other;
}

