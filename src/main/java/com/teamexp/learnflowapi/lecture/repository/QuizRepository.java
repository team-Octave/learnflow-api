package com.teamexp.learnflowapi.lecture.repository;

import com.teamexp.learnflowapi.lecture.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByLessonIdOrderByOrderIndexAsc(Long lessonId);

    List<Quiz> findByLessonIdInOrderByLessonIdAscOrderIndexAsc(List<Long> lessonIds);

    //레슨 삭제 시 해당 레슨에 속한 모든 퀴즈를 함께 삭제하기 위한 메서드.
    @Modifying
    void deleteByLessonId(Long lessonId);
}
