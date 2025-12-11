package com.teamexp.learnflowapi.enrollment.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "lecture_id"})  //복합키
})
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

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

    protected Enrollment() {
    }

    private Enrollment(String userId, Long lectureId) {
        this.userId = userId;
        this.lectureId = lectureId;

        // 생성자 자동 주입 항목
        this.status = EnrollmentStatus.IN_PROGRESS;
        this.progress = 0;
    }

    // 인스턴스 객체 생성
    public static Enrollment create(String userId, Long lectureId) {
        return new Enrollment(userId, lectureId);
    }
//    내 강좌 선택 시 필요한 updatedAt -> 현재 사용 안함 논의 필요
//    public void update() {
//        this.updatedAt = Instant.now();
//    }
    // completedLesson 시 진행률 업데이트
    public void updateProgress(int updateProgress) {
        this.progress = updateProgress;
    }
    // 진행률 100일 때 status 업데이트 용
    public void updateStatus(EnrollmentStatus enrollmentStatus) {
        this.status = enrollmentStatus;
    }
}
