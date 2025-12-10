package com.teamexp.learnflowapi.enrollment.repository;

import com.teamexp.learnflowapi.enrollment.dto.MyEnrollmentResponse;
import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserIdAndLectureId(String userId, Long lectureId);

    Optional<Enrollment> findByUserIdAndLectureId(String userId, Long lectureId);

    @Query(value = """
           SELECT 
               l.id AS lectureId,
               e.enrollment_id AS enrollmentId,
               r.review_id AS reviewId,
               t.file_key AS lectureThumbnail,
               l.title AS lectureTitle,
               e.status AS enrollmentStatus,
               e.progress AS progress,
               e.enrolled_at AS enrolledAt,
               e.updated_at AS updatedAt,
               r.rating AS reviewRating,
               r.content AS reviewContent
           FROM enrollments e
           JOIN lectures l ON e.lecture_id = l.id
           LEFT JOIN review r ON r.enrollment_id = e.enrollment_id
           LEFT JOIN thumbnail t ON t.lecture_id = l.id
           WHERE e.user_id = :userId
           """, nativeQuery = true)
    List<Tuple> findMyEnrollmentsByUserIdNative(@Param("userId") String userId);

    @Query(value = """
    SELECT
        COALESCE(cl.completed_count, 0) AS completedCount,
        COALESCE(tl.total_count, 0) AS totalCount
    FROM enrollments e
    JOIN (
        SELECT c.lecture_id, COUNT(*) AS total_count
        FROM lessons l
        JOIN chapters c ON l.chapter_id = c.id
        GROUP BY c.lecture_id
    ) tl ON tl.lecture_id = e.lecture_id
    LEFT JOIN (
        SELECT enrollment_id, COUNT(*) AS completed_count
        FROM completed_lessons
        GROUP BY enrollment_id
    ) cl ON cl.enrollment_id = e.enrollment_id
    WHERE e.enrollment_id = :enrollmentId
    """, nativeQuery = true)
    Object[] getProgressCounts(@Param("enrollmentId") Long enrollmentId);

    @Query(value = """
        SELECT 
            e.lecture_id AS lectureId, 
            e.enrollment_id AS enrollmentId,
            e.progress AS progress,  -- 진행률을 그대로 사용
            COALESCE(MAX(cl.lesson_id), 0) AS lastCompletedLessonId, 
            COALESCE(GROUP_CONCAT(DISTINCT cl.lesson_id), '') AS completedLessonIds,
            COALESCE(MAX(l.chapter_id), 0) AS lastCompletedLessonChapterId
        FROM enrollments e
        JOIN completed_lessons cl ON cl.enrollment_id = e.enrollment_id
        JOIN lessons l ON l.id = cl.lesson_id
        WHERE e.enrollment_id = :enrollmentId
        GROUP BY e.enrollment_id, e.lecture_id
    """, nativeQuery = true)
    Object selectEnrollment(@Param("enrollmentId") Long enrollmentId);

}
