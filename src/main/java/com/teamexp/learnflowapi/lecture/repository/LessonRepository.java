package com.teamexp.learnflowapi.lecture.repository;

import com.teamexp.learnflowapi.lecture.model.Lesson;

import java.util.List;
import java.util.Optional;

public interface LessonRepository {

    Lesson save(Lesson lesson);

    Optional<Lesson> findById(Long id);

    List<Lesson> findAllById(Iterable<Long> ids);

    void delete(Lesson lesson);

    void deleteById(Long id);

    boolean existsById(Long id);
}
