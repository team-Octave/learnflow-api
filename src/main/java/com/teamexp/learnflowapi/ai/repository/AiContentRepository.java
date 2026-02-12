package com.teamexp.learnflowapi.ai.repository;

import com.teamexp.learnflowapi.ai.model.AiContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiContentRepository extends JpaRepository<AiContent, Long> {

    Optional<AiContent> findByLessonId(Long lessonId);
}
