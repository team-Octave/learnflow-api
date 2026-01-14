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

    public static Approval create(Long lectureId) {
        return new Approval(lectureId);
    }
}
