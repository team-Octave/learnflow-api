package com.teamexp.learnflowapi.content.repository;

import com.teamexp.learnflowapi.content.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz,Long> {

    // 레슨에 달린 문제들 순서대로 조회
    List<Quiz> findAllByLessonIdOrderByOrderIndexAsc(Long lessonId);

    // 레슨 삭제시 같이 삭제
    void deleteByLessonId(Long lessonId);
}
