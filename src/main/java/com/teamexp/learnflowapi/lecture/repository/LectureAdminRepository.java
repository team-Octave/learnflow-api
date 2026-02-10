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
    // 퀴즈 없이 챕터와 레슨만 조회 - 승인 처리 및 AI 작업 트리거 시 성능 최적화를 위해 사용
    Optional<Lecture> findByIdWithChaptersAndLessons(Long lectureId);
}
