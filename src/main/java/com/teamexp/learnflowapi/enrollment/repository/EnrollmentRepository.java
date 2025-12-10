package com.teamexp.learnflowapi.enrollment.repository;

import com.teamexp.learnflowapi.enrollment.dto.MyEnrollmentResponse;
import com.teamexp.learnflowapi.enrollment.model.Enrollment;
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
           FROM enrollment e
           JOIN lectures l ON e.lecture_id = l.id
           LEFT JOIN review r ON r.enrollment_id = e.enrollment_id
           LEFT JOIN thumbnail t ON t.lecture_id = l.id
           WHERE e.user_id = :userId
           """, nativeQuery = true)
    List<MyEnrollmentResponse> findMyEnrollmentsByUserIdNative(@Param("userId") String userId);

}
