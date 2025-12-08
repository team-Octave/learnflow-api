package com.teamexp.learnflowapi.enrollment.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "enrollment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "lecture_id"})  //복합키
})
public class Enrollment {
// TODO 썸네일도 필요함 refactor로 수정 예정, userId Long -> String 수정 예정
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnrollmentStatus status;    // IN_PROGRESS, COMPLETED

    @Column(name = "progress", nullable = false)
    private Integer progress;

    @CreatedDate
    @Column(name = "enrolled_at", columnDefinition = "TIMESTAMP", updatable = false, nullable = false)
    private Instant enrolledAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private Instant updatedAt;

    protected Enrollment() {}

    private Enrollment(Long userId, Long lectureId) {
//      TODO 예외 처리 협의 후 수정 예정
        if (userId == null || userId <= 0L) {throw new IllegalArgumentException("userId cannot be null or zero or negative");}
        if (lectureId == null || lectureId <= 0L) {throw new IllegalArgumentException("lectureId cannot be null or zero or negative");}

        // 생성자 자동 주입 항목
        this.userId = userId;
        this.lectureId = lectureId;
        this.status = EnrollmentStatus.IN_PROGRESS;
        this.progress = 0;
    }

    // 인스턴스 객체 생성
    public static Enrollment create(Long userId, Long lectureId) {
        return new Enrollment(userId, lectureId);
    }

    public void update() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getLectureId() {
        return lectureId;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
