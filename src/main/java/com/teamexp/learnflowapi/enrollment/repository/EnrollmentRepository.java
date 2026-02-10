package com.teamexp.learnflowapi.enrollment.repository;

import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserIdAndLectureId(String userId, Long lectureId);

    Optional<Enrollment> findByUserIdAndLectureId(String userId, Long lectureId);


    List<Enrollment> findByUserId(String userId);

    // 강의별 완강 수 집계 (정산 대상)
    @Query("SELECT e.lectureId AS lectureId, COUNT(e) AS count FROM Enrollment e WHERE e.status = 'COMPLETED' GROUP BY e.lectureId")
    List<LectureSalesProjection> countCompletedEnrollmentsGroupByLectureId();


}
