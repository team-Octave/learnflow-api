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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
    @Query("SELECT t FROM AiTask t " +
        "WHERE t.status = :status " +
        "AND (t.nextAttemptAt IS NULL OR t.nextAttemptAt <= CURRENT_TIMESTAMP) " +
        "ORDER BY t.createdAt ASC")
    List<AiTask> findTasksToProcess(@Param("status") TaskStatus status, Pageable pageable);

    List<AiTask> findByStatusAndUpdatedAtBefore(TaskStatus status, Instant updatedAt);

    Optional<AiTask> findByLessonId(Long lessonId);
}
