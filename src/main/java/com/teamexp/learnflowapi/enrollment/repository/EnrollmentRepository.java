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
        -- [A] 완료된 레슨 ID 목록 (completed_at 오름차순 정렬 추가)
        CASE 
            WHEN COUNT(cl.lesson_id) = 0 THEN NULL 
            ELSE GROUP_CONCAT(DISTINCT cl.lesson_id ORDER BY cl.completed_at ASC) 
        END AS completedLessonIds,
        
        -- [B] 마지막으로 완료된 레슨의 챕터 ID (시간 기반)
        SUBSTRING_INDEX(
            GROUP_CONCAT(l_completed.chapter_id ORDER BY cl.completed_at DESC), 
            ',', 
            1
        ) AS lastCompletedLessonChapterId,
        
        -- [C] 첫 번째 레슨/챕터 ID
        fcl.lecture_first_lesson_id,  
        fcl.lecture_first_chapter_id  
        
    FROM enrollments e
    
    LEFT JOIN completed_lessons cl ON cl.enrollment_id = e.enrollment_id
    LEFT JOIN lessons l_completed ON l_completed.id = cl.lesson_id
    
    -- [C] 첫 번째 레슨/챕터 ID를 찾는 서브쿼리 (ROW_NUMBER() 사용 - MySQL 8.0+)
    LEFT JOIN (
        SELECT
            c_first.lecture_id,
            l_first.id AS lecture_first_lesson_id,
            c_first.id AS lecture_first_chapter_id,
            ROW_NUMBER() OVER (PARTITION BY c_first.lecture_id ORDER BY c_first.chapter_order ASC, l_first.lesson_order ASC) as rn
        FROM chapters c_first
        JOIN lessons l_first ON c_first.id = l_first.chapter_id
    ) fcl ON fcl.lecture_id = e.lecture_id AND fcl.rn = 1
    
    WHERE e.enrollment_id = :enrollmentId
    GROUP BY 
        e.enrollment_id, e.lecture_id, e.progress, 
        fcl.lecture_first_lesson_id, fcl.lecture_first_chapter_id
""", nativeQuery = true)
    Object selectEnrollment(@Param("enrollmentId") Long enrollmentId);

    @Query(value = """
    SELECT
        e.enrollment_id AS enrollmentId,
        
        -- [A] 완료된 레슨 ID 목록 (GROUP_CONCAT으로 복원)
        CASE 
            WHEN COUNT(cl.lesson_id) = 0 THEN NULL 
            ELSE GROUP_CONCAT(DISTINCT cl.lesson_id ORDER BY cl.completed_at ASC) 
        END AS completedLessonIds,
        
        -- [B] 마지막으로 완료된 레슨의 챕터 ID (시간 기반)
        SUBSTRING_INDEX(
            GROUP_CONCAT(l_completed.chapter_id ORDER BY cl.completed_at DESC), 
            ',', 
            1
        ) AS lastCompletedLessonChapterId,
        
        -- [C] 첫 번째 챕터 ID
        fcl.lecture_first_chapter_id AS firstChapterId,  
        
        -- [D] 첫 번째 레슨 ID
        fcl.lecture_first_lesson_id AS firstLessonId
        
    FROM enrollments e
    
    LEFT JOIN completed_lessons cl ON cl.enrollment_id = e.enrollment_id
    LEFT JOIN lessons l_completed ON l_completed.id = cl.lesson_id
    
    -- 첫 번째 레슨/챕터 ID를 찾는 서브쿼리 (ROW_NUMBER() 사용 - MySQL 8.0+)
    LEFT JOIN (
        SELECT
            c_first.lecture_id,
            l_first.id AS lecture_first_lesson_id,
            c_first.id AS lecture_first_chapter_id,
            ROW_NUMBER() OVER (PARTITION BY c_first.lecture_id ORDER BY c_first.chapter_order ASC, l_first.lesson_order ASC) as rn
        FROM chapters c_first
        JOIN lessons l_first ON c_first.id = l_first.chapter_id
    ) fcl ON fcl.lecture_id = e.lecture_id AND fcl.rn = 1
    
    WHERE e.enrollment_id IN :enrollmentIds -- **IN Clause로 N+1 방지**
    GROUP BY 
        e.enrollment_id, e.lecture_id, 
        fcl.lecture_first_lesson_id, fcl.lecture_first_chapter_id
    """, nativeQuery = true)
    List<Tuple> findEnrollmentDetailsBulk(@Param("enrollmentIds") List<Long> enrollmentIds);
}
