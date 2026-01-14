package com.teamexp.learnflowapi.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.List;

@Embeddable
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

    protected ApprovalRejectReason() {}

    public static ApprovalRejectReason from(List<ApprovalRejectType> types) {
        // 승인 허가를 내리는 경우에는 null을 반환
        if (types == null || types.isEmpty()) {
            return null;
        }

        ApprovalRejectReason reason = new ApprovalRejectReason();

        for (ApprovalRejectType type : types) {
            switch (type) {
                case CONTENT_QUALITY_LOW -> reason.contentQualityLow = true;
                case LECTURE_INFO_MISMATCH -> reason.lectureInfoMismatch = true;
                case MEDIA_QUALITY_ISSUE -> reason.mediaQualityIssue = true;
                case POLICY_VIOLATION -> reason.policyViolations = true;
                case OTHER -> reason.other = true;
            }
        }
        return reason;
    }
}

