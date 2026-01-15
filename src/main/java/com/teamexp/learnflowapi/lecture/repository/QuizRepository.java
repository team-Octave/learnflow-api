package com.teamexp.learnflowapi.lecture.repository;

import com.teamexp.learnflowapi.lecture.model.Quiz;

import java.util.List;
import java.util.Optional;

public interface QuizRepository {

    Quiz save(Quiz quiz);
    
    // List<Quiz> saveAll(Iterable<Quiz> quizzes);

    Optional<Quiz> findById(Long id);

    List<Quiz> findAllById(Iterable<Long> ids);

    void delete(Quiz quiz);

    void deleteById(Long id);

    boolean existsById(Long id);

    List<Quiz> findByLessonIdOrderByOrderIndexAsc(Long lessonId);

    List<Quiz> findByLessonIdInOrderByLessonIdAscOrderIndexAsc(List<Long> lessonIds);

    //레슨 삭제 시 해당 레슨에 속한 모든 퀴즈를 함께 삭제하기 위한 메서드.
    void deleteByLessonId(Long lessonId);
}
