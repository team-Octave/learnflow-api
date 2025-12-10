package com.teamexp.learnflowapi.review.service;

import com.teamexp.learnflowapi.enrollment.exception.EnrollmentNotFoundException;
import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.repository.CompletedLessonRepository;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import com.teamexp.learnflowapi.lecture.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.lecture.exception.NotInstructorException;
import com.teamexp.learnflowapi.lecture.exception.SelfReviewNotAllowedException;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatistic;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureStatisticRepository;
import com.teamexp.learnflowapi.review.dto.ReviewRequest;
import com.teamexp.learnflowapi.review.dto.ReviewResponse;
import com.teamexp.learnflowapi.review.exception.NotEnoughProgressException;
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

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private static final int MIN_COMPLETED_LESSON_COUNT = 3;
    private final ReviewRepository reviewRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CompletedLessonRepository completedLessonRepository;
    private final UserRepository userRepository;
    private final LectureStatisticRepository lectureStatisticRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         LectureRepository lectureRepository,
                         EnrollmentRepository enrollmentRepository,
                         CompletedLessonRepository completedLessonRepository,
                         UserRepository userRepository,
                         LectureStatisticRepository lectureStatisticRepository) {
        this.reviewRepository = reviewRepository;
        this.lectureRepository = lectureRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.completedLessonRepository = completedLessonRepository;
        this.userRepository = userRepository;
        this.lectureStatisticRepository = lectureStatisticRepository;
    }

    // 1. 수강평 작성
    @Transactional
    public ReviewResponse createReview(CustomUserPrincipal user, ReviewRequest request) {
        String userId = user.getId();
        // 1. 강의 조회
        Lecture lecture = lectureRepository.findById(request.lectureId())
            .orElseThrow(LectureNotFoundException::new);

        // 2. 강의 생성자 검증 (본인 강의 리뷰 작성 불가)
         if (lecture.getInstructorId().equals(userId)) {
             throw new SelfReviewNotAllowedException();
         }

        // 3. 수강생 검증 (404 예외)
        Enrollment enrollment = enrollmentRepository.findByUserIdAndLectureId(userId, request.lectureId())
            .orElseThrow(EnrollmentNotFoundException::new);

        // 4. 중복 작성 방지
        if (reviewRepository.existsByEnrollment(enrollment)) {
            throw new ReviewAlreadyExistsException();
        }


        // 5. 진도율 검증 (완료된 Lesson 3개 이상)
        int completedCount = completedLessonRepository.countByEnrollmentId(enrollment.getId());
        if (completedCount < MIN_COMPLETED_LESSON_COUNT) {
            throw new NotEnoughProgressException();
        }

        // 6. 리뷰 저장
        Review review = Review.create(enrollment, request.content(), request.rating());
        Review savedReview = reviewRepository.save(review);

        // 7. 통계 업데이트 [추가] 팀원 요청
        updateLectureStatistic(request.lectureId(),request.rating(),true);

        // 8. 실제 강의 제목 사용
        return ReviewResponse.of(savedReview, user.getNickname(), lecture.getTitle());
    }

    // 2. 강의별 리뷰 조회
    public Page<ReviewResponse> getReviewsByLecture(Long lectureId, Pageable pageable) {

        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(LectureNotFoundException::new);

        Page<Review> reviewPage = reviewRepository.findByEnrollment_LectureIdAndStatus(
            lectureId,
            ReviewStatus.POSTED,
            pageable
        );

        // N+1 해결 로직
        Set<String> userIds = reviewPage.stream()
            .map(Review::getUserId)
            .collect(Collectors.toSet());

        Map<String, String> nicknameMap = userRepository.findAllById(userIds).stream()
        .collect(Collectors.toMap(User::getUserId, User::getNickname));

        String lectureTitle = lecture.getTitle();
        return reviewPage.map(review -> {
            String nickname = nicknameMap.getOrDefault(review.getUserId(),"(알 수 없음)");
            return ReviewResponse.of(review, nickname, lectureTitle);
        });
    }

    // 3. 내 리뷰 조회
    public Page<ReviewResponse> getMyReviews(CustomUserPrincipal user, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByEnrollment_UserId(user.getId(), pageable);

        Set<Long> lectureIds = reviewPage.stream()
            .map(Review::getLectureId)
            .collect(Collectors.toSet());

        Map<Long, String> lectureTitleMap = lectureRepository.findAllById(lectureIds).stream()
            .collect(Collectors.toMap(Lecture::getId, Lecture::getTitle));

        return  reviewPage.map(review ->{

            String lectureTitle = lectureTitleMap.getOrDefault(review.getLectureId(),"삭제된 강의");
            return ReviewResponse.of(review, user.getNickname(), lectureTitle);
        });
    }

    // 4. 수강평 삭제
    @Transactional
    public void deleteReview(String userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(ReviewNotFoundException::new);

        if (!review.getUserId().equals(userId)) {
            throw new NotMyReviewException();
        }

        Long lectureId = review.getLectureId();
        Integer rating = review.getRating();

        reviewRepository.delete(review);

        //  통계 업데이트 [추가] 팀원 요청
        updateLectureStatistic(lectureId, rating, false);
    }

    // 5. 강사 답글 등록
    @Transactional
    public void addReply(String userId, Long reviewId, String replyContent) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(ReviewNotFoundException::new);

        Lecture lecture = lectureRepository.findById(review.getLectureId())
            .orElseThrow(LectureNotFoundException::new);

         if (!lecture.getInstructorId().equals(userId)) {
             throw new NotInstructorException();
         }

        review.reply(replyContent);
    }

    // [추가] 팀원 요청: 통계 업데이트 헬퍼 메서드
    private void updateLectureStatistic(Long lectureId, Integer rating, boolean isAdd) {
        LectureStatistic statistic = lectureStatisticRepository.findById(lectureId)
            .orElse(null);
        if (statistic == null) {
            // 통계가 없으면 새로 생성(리뷰 추가인 경우만)
            if (isAdd) {
                // 팀원이 구현할 메서드
                statistic = LectureStatistic.createForNewReview(lectureId, rating);
                lectureStatisticRepository.save(statistic);
            }
            return;
        }

        // 기존 통계 업데이트
        if (isAdd) {
            statistic.addRating(rating);
        }else{
            // 팀원이 구현할 메서드
            statistic.removeRating(rating);
        }

        lectureStatisticRepository.save(statistic);
    }
}
