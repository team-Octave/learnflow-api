package com.teamexp.learnflowapi.ai.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;

@Entity
@Getter
@Table(name = "ai_outbox",
    indexes = {
        @Index(name = "idx_ai_task_poll", columnList = "status, next_attempt_at, created_at"), // 폴링 성능 최적화
        @Index(name = "idx_ai_task_lesson_id", columnList = "lesson_id", unique = true) // 레슨 ID 유니크 인덱스
    }
)
@EntityListeners(AuditingEntityListener.class)
public class AiTask {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // ✨ [수정] DB 레벨 중복 방지
    private Long lessonId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Version
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AiTask() {}

    private AiTask(Long lessonId, TaskStatus status) {
        this.lessonId = lessonId;
        this.status = status;
        this.retryCount = 0;
        this.nextAttemptAt = Instant.now();
    }

    public static AiTask create(Long lessonId) {
        return new AiTask(lessonId, TaskStatus.READY);
    }

    public void changeStatus(TaskStatus status) {
        this.status = status;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public void setNextAttemptAt(Instant nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }
}
