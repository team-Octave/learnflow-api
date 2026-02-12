package com.teamexp.learnflowapi.ai.repository;

import com.teamexp.learnflowapi.ai.model.AiSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AiSummaryRepository extends JpaRepository<AiSummary, Long> {
    Optional<AiSummary> findByLessonId(Long lessonId);
}
