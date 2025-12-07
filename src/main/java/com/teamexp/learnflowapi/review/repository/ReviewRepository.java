package com.teamexp.learnflowapi.review.repository;

import com.teamexp.learnflowapi.review.model.Review;
import com.teamexp.learnflowapi.review.model.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 중복 작성 방지
    boolean existsByUserIdAndLectureId(String userId, Long lectureId);

    // [New] 강의별 리뷰 조회 (상태 필터링 + 페이징)
    // POSTED 상태인 리뷰만 조회하도록 필터링 로직 포함
    Page<Review> findByLectureIdAndStatus(Long lectureId, ReviewStatus status, Pageable pageable);

    // [New] 내 리뷰 조회 (페이징)
    // 내 리뷰는 상태(BLINDED 등) 상관없이 모두 조회
    Page<Review> findByUserId(String userId, Pageable pageable);
}
