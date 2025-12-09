package com.teamexp.learnflowapi.review.service;

import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.repository.CompletedLessonRepository;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.review.dto.ReviewRequest;
import com.teamexp.learnflowapi.review.dto.ReviewResponse;
import com.teamexp.learnflowapi.review.exception.NotMyReviewException;
import com.teamexp.learnflowapi.review.exception.ReviewAlreadyExistsException;
import com.teamexp.learnflowapi.review.exception.ReviewNotFoundException;
import com.teamexp.learnflowapi.review.model.Review;
import com.teamexp.learnflowapi.review.model.ReviewStatus;
import com.teamexp.learnflowapi.review.repository.ReviewRepository;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         LectureRepository lectureRepository,
                         EnrollmentRepository enrollmentRepository,
                         CompletedLessonRepository completedLessonRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.lectureRepository = lectureRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.completedLessonRepository = completedLessonRepository;
        this.userRepository = userRepository;
    }

    // 1. 수강평 작성
    @Transactional
    public ReviewResponse createReview(CustomUserPrincipal user, ReviewRequest request) {
        String userId = user.getId();
        // 1. 강의 존재 확인
        Lecture lecture = lectureRepository.findById(request.lectureId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        // 2. 강의 생성자 검증 (본인 강의 리뷰 작성 불가)
        // TODO: Lecture 엔티티에 getInstructorId() 메서드가 있다고 가정 (String 반환)
        // if (lecture.getInstructorId().equals(userId)) {
        //     throw new IllegalStateException("본인의 강의에는 리뷰를 작성할 수 없습니다.");
        // }

        // 3. 수강생 검증 (Enrollment 존재 여부)
        Enrollment enrollment = enrollmentRepository.findByUserIdAndLectureId(userId, request.lectureId())
            .orElseThrow(() -> new IllegalStateException("수강 신청하지 않은 강의입니다."));

        // 4. 중복 작성 방지
        if (reviewRepository.existsByEnrollment(enrollment)) {
            throw new ReviewAlreadyExistsException();
        }


        // 5. 진도율 검증 (완료된 Lesson 3개 이상)
        int completedCount = completedLessonRepository.countByEnrollmentId(enrollment.getId());
        if (completedCount < 3) {
            throw new IllegalStateException("최소 3개의 레슨을 수강 완료해야 리뷰를 작성할 수 있습니다.");
        }

        // 6. 리뷰 저장
        // 변경 Enrollment 객체 주입
        Review review = Review.create(enrollment, request.content(), request.rating());
        Review savedReview = reviewRepository.save(review);

        // 7. [변경] 실제 닉네임 조회
        return ReviewResponse.of(savedReview, user.getNickname());
    }

    // 2. 강의별 리뷰 조회
    public Page<ReviewResponse> getReviewsByLecture(Long lectureId, Pageable pageable) {
        // 1. POSTED 상태인 리뷰만 페이징 조회
        Page<Review> reviewPage = reviewRepository.findByEnrollment_LectureIdAndStatus(
            lectureId,
            ReviewStatus.POSTED,
            pageable
        );

        // 2. [변경] 리스트 조회 시 각 작성자의 실제 닉네임 매핑
        return reviewPage.map(review -> {
            String nickname = getNickname(review.getUserId());
            return ReviewResponse.of(review, nickname);
        });
    }

    // 3. 내 리뷰 조회
    public Page<ReviewResponse> getMyReviews(CustomUserPrincipal user, Pageable pageable) {
        // user.getId()로 조회
        Page<Review> reviewPage = reviewRepository.findByEnrollment_UserId(user.getId(), pageable);

        // user.getNickname()으로 닉네임 최적화 사용
        return reviewPage.map(review -> ReviewResponse.of(review, user.getNickname()));

    }

    // 4. 수강평 삭제
    @Transactional
    public void deleteReview(String userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(ReviewNotFoundException::new);

        // 작성자 본인 확인
        if (!review.getUserId().equals(userId)) {
            throw new NotMyReviewException();
        }

        // Hard Delete
        reviewRepository.delete(review);
    }

    // 5. 강사 답글 등록
    @Transactional
    public void addReply(String userId, Long reviewId, String replyContent) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(ReviewNotFoundException::new);

        Lecture lecture = lectureRepository.findById(review.getLectureId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        // 강의 생성자 검증 (Ownership)
        // TODO: Lecture 엔티티의 getInstructorId()가 String을 반환하도록 구현되어야 함
        // if (!lecture.getInstructorId().equals(userId)) {
        //     throw new IllegalStateException("해당 강의의 생성자만 답글을 달 수 있습니다.");
        // }

        review.reply(replyContent);
    }

    // [추가] 닉네임 조회 헬퍼 메서드
    private String getNickname(String userId){
        return userRepository.findById(userId)
            .map(User::getNickname)
            .orElse("(알 수 없음)");
    }
}
