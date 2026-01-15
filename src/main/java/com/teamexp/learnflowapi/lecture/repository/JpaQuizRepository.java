package com.teamexp.learnflowapi.lecture.repository;

import com.teamexp.learnflowapi.lecture.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaQuizRepository extends JpaRepository<Quiz, Long>, QuizRepository {

    @Override
    List<Quiz> findByLessonIdOrderByOrderIndexAsc(Long lessonId);

    @Override
    List<Quiz> findByLessonIdInOrderByLessonIdAscOrderIndexAsc(List<Long> lessonIds);

    @Override
    @Modifying
    void deleteByLessonId(Long lessonId);
    

    
}
