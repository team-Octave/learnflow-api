package com.teamexp.learnflowapi.lecture.repository;

import com.teamexp.learnflowapi.lecture.model.Chapter;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository {

    Chapter save(Chapter chapter);

    Optional<Chapter> findById(Long id);

    List<Chapter> findAllById(Iterable<Long> ids);

    void delete(Chapter chapter);

    void deleteById(Long id);

    boolean existsById(Long id);
}
