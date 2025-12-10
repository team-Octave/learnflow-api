package com.teamexp.learnflowapi.lecture.repository;

import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;


import java.util.List;
import java.util.Optional;

public interface LectureRepository  {

    Lecture save(Lecture lecture);

    Optional<Lecture> findById(Long id);

    List<Lecture> findAllById(Iterable<Long> ids);

    Optional<Lecture> findByIdWithChapters(Long id);

    Optional<Lecture> findByIdWithChaptersAndLessons(Long id);

    List<Lecture> findByInstructorId(String instructorId);

    List<Lecture> findByStatus(LectureStatus status);

    List<Lecture> findByCategoryId(Integer categoryId);

    List<Lecture> findByCategoryIdAndStatus(Integer categoryId, LectureStatus status);

    void delete(Lecture lecture);

    boolean existsById(Long id);
}
