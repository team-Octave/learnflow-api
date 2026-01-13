package com.teamexp.learnflowapi.lecture.repository;

import java.util.List;
import java.util.Optional;

import com.teamexp.learnflowapi.lecture.model.Lecture;

public interface LectureAdminRepository {

    // Admin에서 조회할 강의 id 목록을 받아서 강의 목록을 조회
    List<Lecture> findAllByIds(List<Long> lectureIds);

    // Status가 SUBMITTED인 강의 목록을 조회
    List<Lecture> findAllSubmittedLectures();

    // findById - to change status from submitted to available or rejected
    Optional<Lecture> findById(Long lectureId);

    // findByIdWithChaptersAndLessonsAndQuizzes - use check lecture detail
    Optional<Lecture> findByIdWithChaptersAndLessonsAndQuizzes(Long lectureId);
    
}
