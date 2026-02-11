package com.teamexp.learnflowapi.ai.repository;

import com.teamexp.learnflowapi.ai.model.AiTask;
import com.teamexp.learnflowapi.ai.model.TaskStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AiTaskRepository extends JpaRepository<AiTask, Long> {

    /**
     * 처리할 작업 조회 (동시성 제어 적용)
     * - PESSIMISTIC_WRITE: 비관적 락 적용
     * - jakarta.persistence.lock.timeout = -2: Hibernate에서 'SKIP LOCKED' 구문으로 변환됨
     * (락이 걸린 행은 대기하지 않고 즉시 건너뛰어 다음 행을 조회함)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
    @Query("SELECT t FROM AiTask t " +
        "WHERE t.status = :status " +
        "AND (t.nextAttemptAt IS NULL OR t.nextAttemptAt <= CURRENT_TIMESTAMP) " +
        "ORDER BY t.createdAt ASC")
    List<AiTask> findTasksToProcess(@Param("status") TaskStatus status, Pageable pageable);

    List<AiTask> findByStatusAndUpdatedAtBefore(TaskStatus status, Instant updatedAt);

    Optional<AiTask> findByLessonId(Long lessonId);

    @Query("SELECT t.lessonId FROM AiTask t WHERE t.lessonId IN :lessonIds")
    List<Long> findAllLessonIdsByLessonIdIn(@Param("lessonIds") List<Long> lessonIds);
}
