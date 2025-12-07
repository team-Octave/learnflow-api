package com.teamexp.learnflowapi.review.service;

import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.repository.CompletedLessonRepository;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.review.dto.ReviewRequest;
import com.teamexp.learnflowapi.review.dto.ReviewResponse;
import com.teamexp.learnflowapi.review.model.Review;
import com.teamexp.learnflowapi.review.model.ReviewStatus;
import com.teamexp.learnflowapi.review.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CompletedLessonRepository completedLessonRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         LectureRepository lectureRepository,
                         EnrollmentRepository enrollmentRepository,
                         CompletedLessonRepository completedLessonRepository) {
        this.reviewRepository = reviewRepository;
        this.lectureRepository = lectureRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.completedLessonRepository = completedLessonRepository;
    }

    // 1. 수강평 작성
    @Transactional
    public ReviewResponse createReview(String userId, ReviewRequest request) {
        // 1. 강의 존재 확인
        Lecture lecture = lectureRepository.findById(request.lectureId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        // 2. 강의 생성자 검증 (본인 강의 리뷰 작성 불가)
        // TODO: Lecture 엔티티에 getInstructorId() 메서드가 있다고 가정 (String 반환)
        // if (lecture.getInstructorId().equals(userId)) {
        //     throw new IllegalStateException("본인의 강의에는 리뷰를 작성할 수 없습니다.");
        // }

        // 3. 중복 작성 방지
        if (reviewRepository.existsByUserIdAndLectureId(userId, request.lectureId())) {
            throw new IllegalStateException("이미 해당 강의에 대한 리뷰를 작성하셨습니다.");
        }

        // 4. 수강생 검증 (Enrollment 존재 여부)
        Enrollment enrollment = enrollmentRepository.findByUserIdAndLectureId(userId, request.lectureId())
            .orElseThrow(() -> new IllegalStateException("수강 신청하지 않은 강의입니다."));

        // 5. 진도율 검증 (완료된 Lesson 3개 이상)
        int completedCount = completedLessonRepository.countByEnrollmentId(enrollment.getId());
        if (completedCount < 3) {
            throw new IllegalStateException("최소 3개의 레슨을 수강 완료해야 리뷰를 작성할 수 있습니다.");
        }

        // 6. 리뷰 저장
        Review review = Review.create(userId, request.lectureId(), request.content(), request.rating());
        Review savedReview = reviewRepository.save(review);

        // 7. DTO 변환 (닉네임 임시 처리)
        String tempNickname = "User_" + userId.substring(0, 8);
        return ReviewResponse.of(savedReview, tempNickname);
    }

    // 2. 강의별 리뷰 조회
    public Page<ReviewResponse> getReviewsByLecture(Long lectureId, Pageable pageable) {
        // 1. POSTED 상태인 리뷰만 페이징 조회
        Page<Review> reviewPage = reviewRepository.findByLectureIdAndStatus(
            lectureId,
            ReviewStatus.POSTED,
            pageable
        );

        // 2. DTO 변환 (닉네임 임시 처리)
        return reviewPage.map(review -> {
            String tempNickname = "User_" + review.getUserId().substring(0, 8);
            return ReviewResponse.of(review, tempNickname);
        });
    }

    // 3. 내 리뷰 조회
    public Page<ReviewResponse> getMyReviews(String userId, Pageable pageable) {
        // 1. 내 리뷰 조회 (필터링 없이 모두 조회)
        Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);

        // 2. DTO 변환 (내 닉네임은 "Me"로 표시)
        return reviewPage.map(review -> {
            String tempNickname = "Me";
            return ReviewResponse.of(review, tempNickname);
        });
    }

    // 4. 수강평 삭제
    @Transactional
    public void deleteReview(String userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 작성자 본인 확인
        if (!review.getUserId().equals(userId)) {
            throw new IllegalStateException("본인의 리뷰만 삭제할 수 있습니다.");
        }

        // Hard Delete
        reviewRepository.delete(review);
    }

    // 5. 강사 답글 등록
    @Transactional
    public void addReply(String userId, Long reviewId, String replyContent) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        Lecture lecture = lectureRepository.findById(review.getLectureId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        // 강의 생성자 검증 (Ownership)
        // TODO: Lecture 엔티티의 getInstructorId()가 String을 반환하도록 구현되어야 함
        // if (!lecture.getInstructorId().equals(userId)) {
        //     throw new IllegalStateException("해당 강의의 생성자만 답글을 달 수 있습니다.");
        // }

        review.reply(replyContent);
    }
}
