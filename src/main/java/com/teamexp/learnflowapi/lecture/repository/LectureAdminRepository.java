package com.teamexp.learnflowapi.lecture.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.teamexp.learnflowapi.lecture.model.Lecture;

public interface LectureAdminRepository {

    // Admin에서 조회할 강의 id 목록을 받아서 강의 목록을 updatedAt이 오래된 순서로 조회
    Page<Lecture> findAllByIds(List<Long> lectureIds, Pageable pageable);

    // Status가 SUBMITTED인 강의 목록을 updatedAt이 오래된 순서로 조회
    Page<Lecture> findAllSubmittedLectures(Pageable pageable);

    // findById - to change status from submitted to available or rejected
    Optional<Lecture> findById(Long lectureId);

    // findByIdWithChaptersAndLessonsAndQuizzes - use check lecture detail
    Optional<Lecture> findByIdWithChaptersAndLessonsAndQuizzes(Long lectureId);

    
}
