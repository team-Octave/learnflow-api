package com.teamexp.learnflowapi.review.repository;

import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.review.model.Review;
import com.teamexp.learnflowapi.review.model.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 중복 작성 방지
    // 변경: Enrollment 객체 자체로 존재 여부 확인(1인1리뷰 정책 최적화)
    boolean existsByEnrollment(Enrollment enrollment);
    // 변경: Review -> Enrollment -> LectureId 순으로 접근
    Page<Review> findByEnrollment_LectureIdAndStatus(Long LectureId, ReviewStatus status, Pageable pageable);

    // 내 리뷰 조회
    // 변경: Review -> Enrollment -> UserId 순으로 접근
    Page<Review> findByEnrollment_UserId(String userId, Pageable pageable);

    List<Review> findByEnrollment_IdIn(List<Long> enrollmentIds);

}
