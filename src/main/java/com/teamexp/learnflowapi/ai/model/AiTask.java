package com.teamexp.learnflowapi.ai.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ai_outbox",
    indexes = {
        @Index(name = "idx_ai_task_poll", columnList = "status, next_attempt_at, created_at"),
        @Index(name = "idx_ai_task_lesson_id", columnList = "lesson_id", unique = true),
        @Index(name = "idx_ai_task_heartbeat", columnList = "status, last_heartbeat_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class AiTask {

    private static final int MAX_RETRY_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lesson_id", nullable = false, unique = true)
    private Long lessonId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    // Heartbeat tracking
    @Column(name = "worker_id", length = 100)
    private String workerId;

    @Column(name = "last_heartbeat_at")
    private Instant lastHeartbeatAt;

    @Column(name = "current_step", length = 50)
    private String currentStep;

    @Column(name = "progress")
    private Integer progress;

    // Error tracking
    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Version
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // 생성자는 비즈니스 로직(초기 상태 설정)을 담기 위해 private으로 제한
    private AiTask(Long lessonId, TaskStatus status) {
        this.lessonId = lessonId;
        this.status = status;
        this.retryCount = 0;
        this.nextAttemptAt = Instant.now();
    }

    // 팩토리 메서드
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

    /**
     * 최대 재시도 횟수 초과 여부를 엔티티 스스로 판단 (캡슐화)
     */
    public boolean isRetryLimitExceeded() {
        return this.retryCount > MAX_RETRY_COUNT;
    }

    /**
     * 워커에게 작업 할당
     */
    public void assignToWorker(String workerId) {
        this.workerId = workerId;
        this.lastHeartbeatAt = Instant.now();
        this.currentStep = "assigned";
        this.progress = 0;
        this.status = TaskStatus.PROCESSING;
    }

    /**
     * 하트비트 업데이트
     */
    public void updateHeartbeat(String currentStep, Integer progress) {
        this.lastHeartbeatAt = Instant.now();
        this.currentStep = currentStep;
        this.progress = progress;
    }

    /**
     * 에러 정보 설정
     */
    public void setError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 완료 시 클리어
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.currentStep = "completed";
        this.progress = 100;
    }

    /**
     * 워커 할당 해제 (재시도 또는 취소 시)
     */
    public void clearWorker() {
        this.workerId = null;
        this.lastHeartbeatAt = null;
        this.currentStep = null;
        this.progress = null;
    }
}
