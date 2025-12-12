package com.teamexp.learnflowapi.enrollment.repository;

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
               t.file_url AS lectureThumbnail,
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
    Object getProgressCounts(@Param("enrollmentId") Long enrollmentId);

@Query(value = """
    SELECT 
        e.lecture_id AS lectureId, 
        e.enrollment_id AS enrollmentId,
        e.progress AS progress,
        -- 완료된 레슨 ID 목록 (GROUP_CONCAT 사용)
        CASE 
            WHEN COUNT(cl.lesson_id) = 0 THEN NULL 
            ELSE GROUP_CONCAT(DISTINCT cl.lesson_id ORDER BY cl.completed_at ASC) 
        END AS completedLessonIds,
        -- 마지막으로 완료된 레슨의 챕터 ID
        MAX(l_completed.chapter_id) AS lastCompletedLessonChapterId,
        fcl.lecture_first_lesson_id,  -- 강의의 첫 번째 레슨 ID
        fcl.lecture_first_chapter_id  -- 강의의 첫 번째 챕터 ID
    FROM enrollments e
    
    -- 완료된 레슨 정보 조인 (LEFT JOIN으로 수강 시작만 하고 완료가 없는 경우도 처리)
    LEFT JOIN completed_lessons cl ON cl.enrollment_id = e.enrollment_id
    LEFT JOIN lessons l_completed ON l_completed.id = cl.lesson_id
    
    -- 강의의 첫 번째 레슨/챕터 ID를 찾는 서브쿼리 (fcl: First Chapter and Lesson)
    LEFT JOIN (
        SELECT
            c_first.lecture_id,
            l_first.id AS lecture_first_lesson_id,
            c_first.id AS lecture_first_chapter_id
        FROM chapters c_first
        JOIN lessons l_first ON c_first.id = l_first.chapter_id
        
        -- 가장 작은 chapter_order와 lesson_order를 가진 하나의 레코드만 선택
        WHERE (c_first.lecture_id, c_first.chapter_order, l_first.lesson_order) IN (
            SELECT
                c_inner.lecture_id,
                MIN(c_inner.chapter_order) AS min_chapter_order,
                -- 최소 chapter_order를 가진 챕터의 최소 lesson_order를 찾음
                (
                    SELECT MIN(l_inner.lesson_order)
                    FROM lessons l_inner
                    JOIN chapters c_inner2 ON l_inner.chapter_id = c_inner2.id
                    WHERE c_inner2.lecture_id = c_inner.lecture_id 
                    AND c_inner2.chapter_order = MIN(c_inner.chapter_order)
                ) AS min_lesson_order
            FROM chapters c_inner
            GROUP BY c_inner.lecture_id
        )
    ) fcl ON fcl.lecture_id = e.lecture_id
    
    WHERE e.enrollment_id = :enrollmentId
    GROUP BY 
        e.enrollment_id, e.lecture_id, e.progress, 
        fcl.lecture_first_lesson_id, fcl.lecture_first_chapter_id
""", nativeQuery = true)
    Object selectEnrollment(@Param("enrollmentId") Long enrollmentId);

}
