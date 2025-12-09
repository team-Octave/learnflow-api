package com.teamexp.learnflowapi.lecture.repository;

import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaLectureRepository extends JpaRepository<Lecture, Long>, LectureRepository {

    @Override
    @Query("SELECT l FROM Lecture l LEFT JOIN FETCH l.chapters WHERE l.id = :id")
    Optional<Lecture> findByIdWithChapters(@Param("id") Long id);

    @Override
    @Query("SELECT DISTINCT l FROM Lecture l " +
        "LEFT JOIN FETCH l.chapters c " +
        "LEFT JOIN FETCH c.lessons " +
        "WHERE l.id = :id")
    Optional<Lecture> findByIdWithChaptersAndLessons(@Param("id") Long id);

    @Override
    List<Lecture> findByInstructorId(String instructorId);

    @Override
    List<Lecture> findByStatus(LectureStatus status);

    @Override
    List<Lecture> findByCategoryId(Integer categoryId);

    @Override
    List<Lecture> findByCategoryIdAndStatus(Integer categoryId, LectureStatus status);
}
