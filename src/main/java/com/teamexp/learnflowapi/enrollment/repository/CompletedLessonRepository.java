package com.teamexp.learnflowapi.enrollment.repository;

import com.teamexp.learnflowapi.enrollment.model.CompletedLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CompletedLessonRepository extends JpaRepository<CompletedLesson, Long> {

    int countByEnrollmentId(Long enrollmentId);

    boolean existsByEnrollmentIdAndLessonId(Long enrollmentId, Long lessonId);

    List<CompletedLesson> findAllByEnrollmentIdInOrderByCompletedAtAsc(List<Long> enrollmentIds);

    List<CompletedLesson> findAllByEnrollmentIdOrderByCompletedAtAsc(Long enrollmentId);
}