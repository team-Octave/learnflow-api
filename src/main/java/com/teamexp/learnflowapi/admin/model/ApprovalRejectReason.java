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

    public Boolean getContentQualityLow() {
        return contentQualityLow;
    }

    public Boolean getLectureInfoMismatch() {
        return lectureInfoMismatch;
    }

    public Boolean getMediaQualityIssue() {
        return mediaQualityIssue;
    }

    public Boolean getPolicyViolations() {
        return policyViolations;
    }

    public Boolean getOther() {
        return other;
    }

    /**
     * 반려 카테고리 목록을 List<String>으로 반환 (프론트엔드 툴팁용)
     */
    public List<String> toCategories() {
        List<String> categories = new java.util.ArrayList<>();
        if (Boolean.TRUE.equals(contentQualityLow)) {
            categories.add("콘텐츠 내용 부족");
        }
        if (Boolean.TRUE.equals(lectureInfoMismatch)) {
            categories.add("강의 정보 불일치 또는 누락");
        }
        if (Boolean.TRUE.equals(mediaQualityIssue)) {
            categories.add("미디어 품질 문제");
        }
        if (Boolean.TRUE.equals(policyViolations)) {
            categories.add("정책 및 법적 기준 위반");
        }
        if (Boolean.TRUE.equals(other)) {
            categories.add("기타");
        }
        return categories;
    }
}

