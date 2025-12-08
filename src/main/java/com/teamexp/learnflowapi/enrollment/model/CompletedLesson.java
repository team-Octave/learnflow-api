package com.teamexp.learnflowapi.enrollment.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "completed_lesson", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"enrollment_id", "lesson_id"})
})
public class CompletedLesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "completed_lesson_id")
    private Long id;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @CreatedDate
    @Column(name = "completed_at", columnDefinition = "TIMESTAMP", nullable = false)
    private Instant completedAt;

    protected CompletedLesson() {}

    private CompletedLesson(Long enrollmentId, Long lessonId) {
//      TODO 예외 처리 협의 후 수정 예정
        if (enrollmentId == null || enrollmentId <= 0) throw new IllegalArgumentException("enrollmentId cannot be null or zero or negative");
        if (lessonId == null || lessonId <= 0) throw new IllegalArgumentException("lessonId cannot be null or zero or negative");

        this.enrollmentId = enrollmentId;
        this.lessonId = lessonId;
    }

    public Long getId() {
        return id;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public Long getLessonId() {
        return lessonId;
    }
}
