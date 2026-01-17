package com.teamexp.learnflowapi.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.List;

@Entity
@Table(name = "approvals")
@EntityListeners(AuditingEntityListener.class)
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private Long approvalId;

    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "reject_reason", length = 200)
    private String rejectReason;

    @Embedded
    private ApprovalRejectReason rejectCategory;

    // JPA 영속성을 위함
    protected Approval() {}

    // Lecture에서 Approval을 생성할 때, 필수로 받아야 하는 값으로 Approval 생성
    private Approval(Long lectureId) {
        this.lectureId = lectureId;
    }

    /*
    * 반려 사유를 여러개 받을 수 있도록 생성자 추가
    * */
    private Approval(Long lectureId, List<ApprovalRejectType> rejectTypes, String reason) {
        this.lectureId = lectureId;
        this.rejectCategory = ApprovalRejectReason.from(rejectTypes);
        this.rejectReason = reason;
    }

    public static Approval create(Long lectureId) {
        return new Approval(lectureId);
    }

    // 반려 사유를 여러개 받아서 Approval 객체를 생성하는 팩토리메서드
    public static Approval create(Long lectureId, List<ApprovalRejectType> rejectTypes, String reason) {
        return new Approval(lectureId, rejectTypes, reason);
    }

    public Long getApprovalId() {
        return approvalId;
    }

    public Long getLectureId() {
        return lectureId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public ApprovalRejectReason getRejectCategory() {
        return rejectCategory;
    }
}
