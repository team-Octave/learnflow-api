package com.teamexp.learnflowapi.lecture.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.teamexp.learnflowapi.lecture.model.Lecture;

@Repository
public interface JpaLectureAdminRepository extends JpaRepository<Lecture, Long>, LectureAdminRepository {
    
    @Override // Admin에서 조회할 강의 id 목록을 받아서 강의 목록을 조회
    @Query("SELECT l FROM Lecture l " +
    "WHERE l.id IN :lectureIds AND l.deleteFlag = false AND l.status = 'SUBMITTED'")
    List<Lecture> findAllByIds(List<Long> lectureIds);

    @Override // Status가 SUBMITTED인 강의 목록을 조회
    @Query("SELECT l FROM Lecture l WHERE l.status = 'SUBMITTED' AND l.deleteFlag = false")
    List<Lecture> findAllSubmittedLectures();
    
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
}

