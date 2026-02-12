package com.teamexp.learnflowapi.review.service;

import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.repository.CompletedLessonRepository;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import com.teamexp.learnflowapi.lecture.exception.NotInstructorException;
import com.teamexp.learnflowapi.lecture.exception.SelfReviewNotAllowedException;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatistic;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureStatisticRepository;
import com.teamexp.learnflowapi.review.dto.ReviewRequest;
import com.teamexp.learnflowapi.review.exception.NotEnoughProgressException;
import com.teamexp.learnflowapi.review.exception.NotMyReviewException;
import com.teamexp.learnflowapi.review.model.Review;
import com.teamexp.learnflowapi.review.repository.ReviewRepository;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    ReviewService reviewService;

    @Mock
    LectureRepository lectureRepository;

    @Mock
    EnrollmentRepository enrollmentRepository;

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    LectureStatisticRepository lectureStatisticRepository;

    @Mock
    CompletedLessonRepository completedLessonRepository;

    @Mock
    UserRepository userRepository;

    @Captor
    ArgumentCaptor<Review> reviewCaptor;

    @Test
    @DisplayName("tc26. 리뷰 생성 실패(본인 강의) - SelfReviewNotAllowedException + save() 미호출")
    void tc26_create_review_fail_self_lecture() {
        // given
        String userId = "user-1";
        Long lectureId = 10L;

        CustomUserPrincipal user = mock(CustomUserPrincipal.class);
        given(user.getId()).willReturn(userId);

        ReviewRequest request = new ReviewRequest(lectureId, 5, "content");

        Lecture lecture = mock(Lecture.class);
        given(lecture.getInstructorId()).willReturn(userId);
        given(lectureRepository.findById(lectureId)).willReturn(Optional.of(lecture));

        assertThatThrownBy(() -> reviewService.createReview(user, request))
                .isInstanceOf(SelfReviewNotAllowedException.class);

        verify(enrollmentRepository, never()).findByUserIdAndLectureId(anyString(), anyLong());
        verify(reviewRepository, never()).save(any());
        verify(reviewRepository, never()).existsByEnrollment(any());
        verify(lectureStatisticRepository, never()).save(any());
    }

    @Test
    @DisplayName("리뷰 생성 실패(진도 미달)")
    void tc27_create_review_fail_not_enough_progress() {
        // given
        String userId = "user-2";
        Long lectureId = 10L;

        CustomUserPrincipal user = mock(CustomUserPrincipal.class);
        given(user.getId()).willReturn(userId);

        ReviewRequest request = new ReviewRequest(lectureId, 5, "content");

        Lecture lecture = mock(Lecture.class);
        given(lecture.getInstructorId()).willReturn("instructor-1"); // != userId
        given(lectureRepository.findById(lectureId)).willReturn(Optional.of(lecture));

        Enrollment enrollment = mock(Enrollment.class);
        given(enrollment.getId()).willReturn(77L);
        given(enrollmentRepository.findByUserIdAndLectureId(userId, lectureId))
                .willReturn(Optional.of(enrollment));

        given(reviewRepository.existsByEnrollment(enrollment)).willReturn(false);

        // 진도 미달: completedCount=2 (<3)
        given(completedLessonRepository.countByEnrollmentId(77L)).willReturn(2);

        assertThatThrownBy(() -> reviewService.createReview(user, request))
                .isInstanceOf(NotEnoughProgressException.class);

        verify(reviewRepository, never()).save(any());
        verify(lectureStatisticRepository, never()).save(any());
    }


    @Test
    @DisplayName("리뷰 삭제 실패(내 리뷰 아님)")
    void tc28_delete_review_fail_not_mine() {
        // given
        String userId = "user-1";
        Long reviewId = 100L;

        Review review = mock(Review.class);
        given(review.getUserId()).willReturn("other-user"); // != userId
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.deleteReview(userId, reviewId))
                .isInstanceOf(NotMyReviewException.class);

        verify(reviewRepository, never()).delete(any(Review.class));
        verify(lectureStatisticRepository, never()).save(any());
    }

    @Test
    @DisplayName("강사 답글 등록 실패(강사 아님)")
    void tc29_add_reply_fail_not_instructor() {
        // given
        String userId = "user-2";
        Long reviewId = 100L;

        Review review = mock(Review.class);
        given(review.getLectureId()).willReturn(10L);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        Lecture lecture = mock(Lecture.class);
        given(lecture.getInstructorId()).willReturn("instructor-999"); // != userId
        given(lectureRepository.findById(10L)).willReturn(Optional.of(lecture));

        assertThatThrownBy(() -> reviewService.addReply(userId, reviewId, "reply"))
                .isInstanceOf(NotInstructorException.class);

        verify(review, never()).reply(anyString());
    }


    @Test
    @DisplayName("리뷰 생성 성공")
    void tc30_create_review_success_minimum() {
        // given
        String userId = "user-2";
        Long lectureId = 10L;

        CustomUserPrincipal user = mock(CustomUserPrincipal.class);
        given(user.getId()).willReturn(userId);
        given(user.getNickname()).willReturn("nick");

        ReviewRequest request = new ReviewRequest(lectureId, 5, "good");

        Lecture lecture = mock(Lecture.class);
        given(lecture.getInstructorId()).willReturn("instructor-1"); // != userId
        given(lecture.getTitle()).willReturn("lecture-title");
        given(lectureRepository.findById(lectureId)).willReturn(Optional.of(lecture));

        Enrollment enrollment = mock(Enrollment.class);
        given(enrollment.getId()).willReturn(77L);
        given(enrollmentRepository.findByUserIdAndLectureId(userId, lectureId))
                .willReturn(Optional.of(enrollment));

        given(reviewRepository.existsByEnrollment(enrollment)).willReturn(false);
        given(completedLessonRepository.countByEnrollmentId(77L)).willReturn(3); // >=3

        Review savedReview = mock(Review.class);
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

        // 통계 업데이트 내부에서 findById -> save 호출됨
        LectureStatistic statistic = mock(LectureStatistic.class);
        given(lectureStatisticRepository.findById(lectureId)).willReturn(Optional.of(statistic));

        // when
        reviewService.createReview(user, request);

        // then
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(lectureStatisticRepository).findById(lectureId);
        verify(lectureStatisticRepository, times(1)).save(any(LectureStatistic.class));
    }

}
