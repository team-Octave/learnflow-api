package com.teamexp.learnflowapi.enrollment.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "completed_lessons", uniqueConstraints = {
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
        this.enrollmentId = enrollmentId;
        this.lessonId = lessonId;
    }

    public static CompletedLesson createCompletedLesson(Long enrollmentId, Long lessonId) {
        return new CompletedLesson(enrollmentId, lessonId);
    }

}
