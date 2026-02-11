package com.teamexp.learnflowapi.enrollment.service;


import com.teamexp.learnflowapi.auth.exception.UserNotFoundException;
import com.teamexp.learnflowapi.content.repository.ThumbnailRepository;
import com.teamexp.learnflowapi.enrollment.dto.CreateCompletedLessonRequest;
import com.teamexp.learnflowapi.enrollment.dto.CreateEnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.MyEnrollmentResponse;
import com.teamexp.learnflowapi.enrollment.dto.SelectEnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.SelectEnrollmentResponse;
import com.teamexp.learnflowapi.enrollment.exception.*;
import com.teamexp.learnflowapi.enrollment.model.CompletedLesson;
import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.model.EnrollmentStatus;
import com.teamexp.learnflowapi.enrollment.repository.CompletedLessonRepository;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import com.teamexp.learnflowapi.lecture.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.lecture.exception.LectureStatusInvalidException;
import com.teamexp.learnflowapi.lecture.exception.LessonNotFoundException;
import com.teamexp.learnflowapi.lecture.model.*;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureStatisticRepository;
import com.teamexp.learnflowapi.membership.model.Membership;
import com.teamexp.learnflowapi.membership.repository.MembershipRepository;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.teamexp.learnflowapi.review.model.Review;
import com.teamexp.learnflowapi.review.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final CompletedLessonRepository completedLessonRepository;
    private final LectureRepository lectureRepository;
    private final LectureStatisticRepository lectureStatisticRepository;
    @SuppressWarnings("unused")  // TODO: 모든 thumbnail 관련 코드 정리 후 제거
    @Deprecated  // thumbnailUrl은 이제 Lecture.thumbnailUrl에서 직접 사용
    private final ThumbnailRepository thumbnailRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CompletedLessonRepository completedLessonRepository,
                             LectureRepository lectureRepository,
                             LectureStatisticRepository lectureStatisticRepository, ThumbnailRepository thumbnailRepository, ReviewRepository reviewRepository, UserRepository userRepository, MembershipRepository membershipRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.completedLessonRepository = completedLessonRepository;
        this.lectureRepository = lectureRepository;
        this.lectureStatisticRepository = lectureStatisticRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    // enrollment 생성
    public void createEnrollment(String userId, CreateEnrollmentRequest request) {

        // Lecture 확인
        Lecture lecture = lectureRepository.findById(request.lectureId()).orElseThrow(LectureNotFoundException::new);
        // Lecture Status확인
        if (!lecture.getStatus().equals(LectureStatus.AVAILABLE)) throw new LectureStatusInvalidException();
        // 자신의 강좌 수강 방지
        if (userId.equals(lecture.getInstructorId())) throw new SelfEnrollmentNotAllowedException();
        // 생성된 수강 확인
        if (enrollmentRepository.existsByUserIdAndLectureId(userId, request.lectureId()))
            throw new EnrollmentAlreadyExistsException();

        enrollmentRepository.save(Enrollment.create(userId, request.lectureId()));

        // 통계 업데이트
        updateEnrollmentCount(request.lectureId(), true);
    }

    // lesson 완료
    public void createCompletedLesson(String userId, CreateCompletedLessonRequest request) {

        // 1. Enrollment 조회 (기존 existsById 대신 findById로 엔티티를 한 번에 가져옵니다)
        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(EnrollmentNotFoundException::new);

        // 2. 유저 검증 (가져온 enrollment 객체로 즉시 확인)
        if (!Objects.equals(enrollment.getUserId(), userId)) {
            throw new EnrollmentAccessDeniedException();
        }

        // 3. [핵심 추가] 해당 강의 정보를 가져와서, 요청한 레슨(lessonId)이 진짜 이 강의에 있는지 확인!
        Lecture lecture = lectureRepository.findByIdWithChaptersAndLessons(enrollment.getLectureId())
                .orElseThrow(LectureNotFoundException::new);

        boolean isLessonInLecture = lecture.getChapters().stream()
                .flatMap(chapter -> chapter.getLessons().stream())
                .anyMatch(lesson -> lesson.getId().equals(request.lessonId()));

        if (!isLessonInLecture) {
            // "이 강의에는 해당 레슨이 없습니다" (다른 강의의 레슨 ID거나 없는 번호)
            throw new LessonNotFoundException();
        }

        // 4. 이미 완료한 레슨인지 중복 확인
        if (completedLessonRepository.existsByEnrollmentIdAndLessonId(request.enrollmentId(), request.lessonId())) {
            throw new CompletedLessonAlreadyExistsException();
        }

        // 5. completedLesson 생성 및 저장
        CompletedLesson completedLesson = CompletedLesson
                .createCompletedLesson(request.enrollmentId(), request.lessonId());
        completedLessonRepository.save(completedLesson);

        // 6. 진행률 업데이트
        updateProgress(request);
    }

    // 유저의 수강 목록 조회
    @Transactional(readOnly = true)
    public List<MyEnrollmentResponse> getEnrollments(String userId) {

        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);

        if (enrollments.isEmpty()) {
            return List.of();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        List<Long> enrollmentIds = enrollments.stream().map(Enrollment::getId).toList();
        List<Long> lectureIds = enrollments.stream().map(Enrollment::getLectureId).distinct().toList();

        Map<Long, Lecture> lectureMap = lectureRepository.findAllById(lectureIds).stream()
                .collect(Collectors.toMap(Lecture::getId, Function.identity()));

        // thumbnailUrl은 이제 Lecture.thumbnailUrl에서 직접 사용 (thumbnail 테이블 의존 제거)
        // @Deprecated: thumbnailRepository.findAllByLectureIdIn(lectureIds) 사용 제거

        Map<Long, Review> reviewMap = reviewRepository.findByEnrollment_IdIn(enrollmentIds).stream()
                .collect(Collectors.toMap(
                        review -> review.getEnrollment().getId(),
                        Function.identity()
                ));

        Map<Long, List<CompletedLesson>> completedLessonMap =
                completedLessonRepository.findAllByEnrollmentIdInOrderByCompletedAtAsc(enrollmentIds).stream()
                        .collect(Collectors.groupingBy(CompletedLesson::getEnrollmentId));

        return enrollments.stream()
                .map(enrollment -> {
                    Lecture lecture = lectureMap.get(enrollment.getLectureId());
                    if (lecture == null) {
                        throw new LectureNotFoundException();
                    }
                    // [1] 임시 변수 선언 (이 줄이 없어서 에러가 난 겁니다!)
                    // 나중에 User 도메인이 완성되면 실제 로직으로 교체할 예정
                    boolean hasActiveMembership = checkUserMembership(user.getUserId()); // 일단 '멤버십 있음(true)'으로 가정


                    Review review = reviewMap.get(enrollment.getId());
                    List<CompletedLesson> myCompletedLessons = completedLessonMap.getOrDefault(enrollment.getId(), List.of());

                    List<Long> completedLessonIds = myCompletedLessons.stream()
                            .map(CompletedLesson::getLessonId)
                            .toList();
                    Long lastCompletedChapterId = calculateLastCompletedChapterId(lecture, myCompletedLessons);
                    Long firstChapterId = getFirstChapterId(lecture);
                    Long firstLessonId = getFirstLessonId(lecture);

                    boolean isAccessible = lecture.isFreeLecture() || hasActiveMembership;

                    return new MyEnrollmentResponse(
                            lecture.getId(),
                            enrollment.getId(),
                            lecture.getPaymentType(),
                            review != null ? review.getId() : null,
                            lecture.getThumbnailUrl(),  // Lecture 엔티티에서 직접 thumbnailUrl 사용
                            lecture.getTitle(),
                            enrollment.getStatus(),
                            enrollment.getProgress(),
                            isAccessible,
                            enrollment.getEnrolledAt(),
                            enrollment.getUpdatedAt(),
                            review != null ? review.getRating() : null,
                            review != null ? review.getContent() : null,
                            completedLessonIds,           // List<Long>
                            lastCompletedChapterId,       // Long
                            firstChapterId,               // Long
                            firstLessonId

                    );

                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    private boolean checkUserMembership(String userId) {
        Membership membership = membershipRepository.findByUserId(userId).orElseThrow(UserNotEnrolledException::new);

        if(!membership.isActive()){
            throw new MembershipExpiredException();
        }

        return true; // 일단 테스트를 위해 true로 둡니다.
    }

    private Long calculateLastCompletedChapterId(Lecture lecture, List<CompletedLesson> completedLessons) {
        if (completedLessons.isEmpty()) return null;


        Long lastLessonId = completedLessons.get(completedLessons.size() - 1).getLessonId();

        return lecture.getChapters().stream()
                .filter(chapter -> chapter.getLessons().stream()
                        .anyMatch(lesson -> lesson.getId().equals(lastLessonId)))
                .findFirst()
                .map(Chapter::getId)
                .orElse(null);
    }

    private Long getFirstChapterId(Lecture lecture) {
        return lecture.getChapters().stream()
                .min(Comparator.comparing(Chapter::getChapterOrder))
                .map(Chapter::getId)
                .orElse(null);
    }

    private Long getFirstLessonId(Lecture lecture) {
        return lecture.getChapters().stream()
                .min(Comparator.comparing(Chapter::getChapterOrder)) // 첫 번째 챕터 찾기
                .flatMap(chapter -> chapter.getLessons().stream()
                        .min(Comparator.comparing(Lesson::getLessonOrder))) // 그 챕터의 첫 번째 레슨 찾기
                .map(Lesson::getId)
                .orElse(null);
    }


    @Transactional(readOnly = true)
    public SelectEnrollmentResponse selectEnrollment(String userId, SelectEnrollmentRequest request) {

        validUser(userId, request.enrollmentId());
        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId()).
                orElseThrow(EnrollmentNotFoundException::new);

        Lecture lecture = lectureRepository.findById(enrollment.getLectureId())
                .orElseThrow(LectureNotFoundException::new);

        List<CompletedLesson> completedLessons = completedLessonRepository
                .findAllByEnrollmentIdOrderByCompletedAtAsc(enrollment.getId());

        List<Long> completedLessIds = completedLessons.stream()
                .map(CompletedLesson::getLessonId)
                .toList();

        Long lastCompletedLessonChapterId = calculateLastCompletedChapterId(lecture, completedLessons);
        Long firstChapterId = getFirstChapterId(lecture);
        Long firstLessonId = getFirstLessonId(lecture);

        return new SelectEnrollmentResponse(
                enrollment.getId(),
                lecture.getId(),
                enrollment.getProgress(),
                completedLessIds,
                lastCompletedLessonChapterId,
                firstChapterId,
                firstLessonId
        );
    }

    // 선택한 강좌 삭제
    public void deleteEnrollment(String userId, SelectEnrollmentRequest request) {

        // 유저 검증 및 lectureId 조회
        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(EnrollmentNotFoundException::new);

        if (!Objects.equals(enrollment.getUserId(), userId)) {
            throw new EnrollmentAccessDeniedException();
        }

        Long lectureId = enrollment.getLectureId();

        try {
            enrollmentRepository.deleteById(request.enrollmentId());
        } catch (EmptyResultDataAccessException e) {
            throw new EnrollmentNotFoundException();
        }

        // 통계 업데이트
        updateEnrollmentCount(lectureId, false);
    }

    private void validUser(String userId, Long enrollmentId) {

        Enrollment requestEnrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(EnrollmentNotFoundException::new);

        if (!Objects.equals(requestEnrollment.getUserId(), userId)) {
            throw new EnrollmentAccessDeniedException();
        }
    }

    private void updateProgress(CreateCompletedLessonRequest request) {
        Enrollment requestEnrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(EnrollmentNotFoundException::new);

        Lecture lecture = lectureRepository.findById(requestEnrollment.getLectureId())
                .orElseThrow(LectureNotFoundException::new);

        int totalLessonCount = lecture.getTotalLessonCount();

        int completedLessonCount = completedLessonRepository.countByEnrollmentId(requestEnrollment.getId());

        int updateProgress = 0;
        if (totalLessonCount > 0) {
            updateProgress = (int) Math.round((double) completedLessonCount / totalLessonCount * 100);
        }

        requestEnrollment.updateProgress(updateProgress);

        if (updateProgress == 100 && requestEnrollment.getStatus() == EnrollmentStatus.IN_PROGRESS) {
            requestEnrollment.updateStatus(EnrollmentStatus.COMPLETED);
        }
    }

    // 통계 업데이트 헬퍼 메서드
    // Lecture 생성 시 LectureStatistic이 함께 생성되므로 항상 존재해야 함
    // 낙관적 잠금 충돌 시 재시도 로직 포함
    private void updateEnrollmentCount(Long lectureId, boolean isAdd) {
        final int MAX_RETRIES = 3;
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                LectureStatistic statistic = lectureStatisticRepository.findById(lectureId)
                        .orElseThrow(LectureNotFoundException::new);

                // 통계 업데이트
                if (isAdd) {
                    statistic.addEnrollment();
                } else {
                    statistic.removeEnrollment();
                }

                lectureStatisticRepository.save(statistic);
                return; // 성공 시 종료
            } catch (OptimisticLockException e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    log.error("Failed to update LectureStatistic enrollment count after {} retries for lectureId: {}",
                            MAX_RETRIES, lectureId, e);
                    throw e;
                }
                // 짧은 대기 후 재시도
                try {
                    Thread.sleep(50L * retryCount); // 50ms, 100ms, 150ms
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
    }

    private void validateEnrolledMembership(boolean isEnrolled) {
        if (!isEnrolled) {
            throw new UserNotEnrolledException();
        }
    }
}
