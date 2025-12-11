package com.teamexp.learnflowapi.content.repository;

import com.teamexp.learnflowapi.content.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByLessonIdOrderByOrderIndexAsc(Long lessonId);

    void deleteByLessonId(Long lessonId);
}
