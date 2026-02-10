package com.teamexp.learnflowapi.lecture.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.teamexp.learnflowapi.lecture.model.Lecture;

@Repository
public interface JpaLectureAdminRepository extends JpaRepository<Lecture, Long>, LectureAdminRepository {
    
    @Override // Admin에서 조회할 강의 id 목록을 받아서 updatedAt이 오래된 강의 목록을 조회
    @Query("SELECT l FROM Lecture l " +
    "WHERE l.id IN :lectureIds AND l.deleteFlag = false AND l.status = 'SUBMITTED' " +
    "ORDER BY l.updatedAt ASC NULLS LAST")
    Page<Lecture> findAllByIds(List<Long> lectureIds, Pageable pageable);

    @Override // Status가 SUBMITTED인 updatedAt이 오래된 강의 목록을 조회
    @Query("SELECT l FROM Lecture l WHERE l.status = 'SUBMITTED' AND l.deleteFlag = false " +
    "ORDER BY l.updatedAt ASC NULLS LAST")
    Page<Lecture> findAllSubmittedLectures(Pageable pageable);
    
    @Override // findById - to change status from submitted to available or rejected
    @Query("SELECT l FROM Lecture l " + 
    "WHERE l.id = :lectureId AND l.deleteFlag = false AND l.status = 'SUBMITTED'")
    Optional<Lecture> findById(Long lectureId);

    @Override
    @Query("SELECT DISTINCT l FROM Lecture l " +
        "LEFT JOIN FETCH l.chapters c " +
        "LEFT JOIN FETCH c.lessons ls " +
        "LEFT JOIN FETCH ls.quizzes " +
        "WHERE l.id = :lectureId AND l.deleteFlag = false AND l.status = 'SUBMITTED'")
    Optional<Lecture> findByIdWithChaptersAndLessonsAndQuizzes(Long lectureId);

    @Override
    @Query("SELECT DISTINCT l FROM Lecture l " +
        "LEFT JOIN FETCH l.chapters c " +
        "LEFT JOIN FETCH c.lessons ls " +
        "WHERE l.id = :lectureId AND l.deleteFlag = false AND l.status = 'SUBMITTED'")
    Optional<Lecture> findByIdWithChaptersAndLessons(@Param("lectureId") Long lectureId);
}


