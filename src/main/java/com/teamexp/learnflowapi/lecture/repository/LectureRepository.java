package com.teamexp.learnflowapi.lecture.repository;

import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureLevel;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LectureRepository  {

    Lecture save(Lecture lecture);

    Optional<Lecture> findById(Long id);

    List<Lecture> findAllById(Iterable<Long> ids);

    Optional<Lecture> findByIdWithChapters(Long id);

    Optional<Lecture> findByIdWithChaptersAndLessons(Long id);

    List<Lecture> findByInstructorId(String instructorId);

    Page<Lecture> findByInstructorId(String instructorId, Pageable pageable);

    Page<Lecture> findByInstructorIdOrderByUpdatedAtDesc(String instructorId, Pageable pageable);

    List<Lecture> findByStatus(LectureStatus status);

    List<Lecture> findByCategoryId(Integer categoryId);

    List<Lecture> findByCategoryIdAndStatus(Integer categoryId, LectureStatus status);

    List<Lecture> findByCategoryIdAndStatusAndDeleteFlagFalse(Integer categoryId, LectureStatus status);

    Page<Lecture> findByFiltersWithStats(Integer categoryId, LectureLevel level, LectureStatus status, String sortBy, Pageable pageable);

    Page<Lecture> findAllWithStatsForAdmin(String sortBy, Pageable pageable);

    void delete(Lecture lecture);

    boolean existsById(Long id);

    void flush();
}
