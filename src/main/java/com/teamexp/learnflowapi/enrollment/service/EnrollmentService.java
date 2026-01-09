package com.teamexp.learnflowapi.enrollment.service;

import com.teamexp.learnflowapi.enrollment.dto.CreateCompletedLessonRequest;
import com.teamexp.learnflowapi.enrollment.dto.CreateEnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.MyEnrollmentResponse;
import com.teamexp.learnflowapi.enrollment.dto.SelectEnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.SelectEnrollmentResponse;
import com.teamexp.learnflowapi.enrollment.exception.CompletedLessonAlreadyExistsException;
import com.teamexp.learnflowapi.enrollment.exception.EnrollmentAccessDeniedException;
import com.teamexp.learnflowapi.enrollment.exception.EnrollmentAlreadyExistsException;
import com.teamexp.learnflowapi.enrollment.exception.EnrollmentNotFoundException;
import com.teamexp.learnflowapi.enrollment.exception.SelfEnrollmentNotAllowedException;
import com.teamexp.learnflowapi.enrollment.model.CompletedLesson;
import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.model.EnrollmentStatus;
import com.teamexp.learnflowapi.enrollment.repository.CompletedLessonRepository;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import com.teamexp.learnflowapi.lecture.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.lecture.exception.LectureStatusInvalidException;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatistic;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureStatisticRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CompletedLessonRepository completedLessonRepository,
                             LectureRepository lectureRepository,
                             LectureStatisticRepository lectureStatisticRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.completedLessonRepository = completedLessonRepository;
        this.lectureRepository = lectureRepository;
        this.lectureStatisticRepository = lectureStatisticRepository;
    }

    // enrollment 생성
    public void createEnrollment(String userId , CreateEnrollmentRequest request) {

        // Lecture 확인
        Lecture lecture = lectureRepository.findById(request.lectureId()).orElseThrow(LectureNotFoundException::new);
        // Lecture Status확인
        if (!lecture.getStatus().equals(LectureStatus.AVAILABLE)) throw new LectureStatusInvalidException();
        // 자신의 강좌 수강 방지
        if (userId.equals(lecture.getInstructorId())) throw new SelfEnrollmentNotAllowedException();
        // 생성된 수강 확인
        if (enrollmentRepository.existsByUserIdAndLectureId(userId, request.lectureId())) throw new EnrollmentAlreadyExistsException();

        enrollmentRepository.save(Enrollment.create(userId, request.lectureId()));

        // 통계 업데이트
        updateEnrollmentCount(request.lectureId(), true);
    }

    // lesson 완료
    public void createCompletedLesson(String userId , CreateCompletedLessonRequest request) {

        // 요청 enrollment 검증
        if (!enrollmentRepository.existsById(request.enrollmentId())) throw new EnrollmentNotFoundException();
        // enrollment에 있는 userId와 현재 로그인 userId 검증
        validUser(userId, request.enrollmentId());
        // completed lesson 중복 확인
        if (completedLessonRepository.existsByEnrollmentIdAndLessonId(request.enrollmentId(), request.lessonId())) throw new CompletedLessonAlreadyExistsException();

        // completedLesson 생성
        CompletedLesson completedLesson = CompletedLesson
                .createCompletedLesson(request.enrollmentId(), request.lessonId());
        completedLessonRepository.save(completedLesson);

        // 진행률 업데이트
        updateProgress(request);
    }

    // 유저의 수강 목록 조회
    @Transactional(readOnly = true)
    public List<MyEnrollmentResponse> getEnrollments(String userId) {

//        List<Tuple> result = enrollmentRepository.findMyEnrollmentsByUserIdNative(userId);
//
//        List<MyEnrollmentResponse> responses = result.stream()
//                .map(tuple -> new MyEnrollmentResponse(
//                        (Long) tuple.get("lectureId"),
//                        (Long) tuple.get("enrollmentId"),
//                        tuple.get("reviewId") != null ? (Long) tuple.get("reviewId") : null,
//                        (String) tuple.get("lectureThumbnail"),
//                        (String) tuple.get("lectureTitle"),
//                        EnrollmentStatus.valueOf((String) tuple.get("enrollmentStatus")),
//                        (Integer) tuple.get("progress"),
//                        ((Timestamp) tuple.get("enrolledAt")).toInstant(),
//                        ((Timestamp) tuple.get("updatedAt")).toInstant(),
//                        (Integer) tuple.get("reviewRating"),
//                        (String) tuple.get("reviewContent")
//                )).toList();
//        return responses;
        // 1. [1단계 쿼리] 기본 정보 조회
        List<Tuple> basicResults = enrollmentRepository.findMyEnrollmentsByUserIdNative(userId);

        // 2. 모든 enrollmentId를 추출
        List<Long> enrollmentIds = basicResults.stream()
                .map(tuple -> (Long) tuple.get("enrollmentId"))
                .toList();

        if (enrollmentIds.isEmpty()) {
            return List.of();
        }

        // 3. [2단계 쿼리] 상세 진행 정보 Bulk 조회
        List<Tuple> detailResults = enrollmentRepository.findEnrollmentDetailsBulk(enrollmentIds);

        // 4. 상세 정보를 Map으로 변환 (O(N) 성능 확보)
        Map<Long, Tuple> detailMap = detailResults.stream()
                .collect(Collectors.toMap(
                        tuple -> (Long) tuple.get("enrollmentId"),
                        Function.identity()
                ));

        // 5. 기본 정보와 상세 정보를 합쳐 최종 DTO 생성
        List<MyEnrollmentResponse> responses = basicResults.stream()
                .map(basicTuple -> {
                    Long enrollmentId = (Long) basicTuple.get("enrollmentId");
                    Tuple detailTuple = detailMap.get(enrollmentId);

                    // 파싱 및 합치기
                    List<Long> completedLessIds = null;
                    Long lastCompletedLessonChapterId = null;
                    Long firstLessonId = null;
                    Long firstChapterId = null;

                    if (detailTuple != null) {
                        // completedLessonIds (String.split() 사용)
                        String completedLessonIdsString = detailTuple.get("completedLessonIds", String.class);
                        completedLessIds = parseCommaSeparatedIds(completedLessonIdsString);

                        // lastCompletedLessonChapterId (String -> Long 파싱)
                        String lastCompletedChapterIdString = detailTuple.get("lastCompletedLessonChapterId", String.class);
                        lastCompletedLessonChapterId = parseLongOrNull(lastCompletedChapterIdString);

                        // firstLessonId / firstChapterId (Number 캐스팅)
                        firstLessonId = detailTuple.get("firstLessonId", Number.class) != null
                                ? detailTuple.get("firstLessonId", Number.class).longValue() : null;
                        firstChapterId = detailTuple.get("firstChapterId", Number.class) != null
                                ? detailTuple.get("firstChapterId", Number.class).longValue() : null;
                    }

                    // 최종 MyEnrollmentResponse 객체 생성
                    return new MyEnrollmentResponse(
                            (Long) basicTuple.get("lectureId"),
                            enrollmentId,
                            basicTuple.get("reviewId") != null ? (Long) basicTuple.get("reviewId") : null,
                            (String) basicTuple.get("lectureThumbnail"),
                            (String) basicTuple.get("lectureTitle"),
                            EnrollmentStatus.valueOf((String) basicTuple.get("enrollmentStatus")),
                            (Integer) basicTuple.get("progress"),
                            ((Timestamp) basicTuple.get("enrolledAt")).toInstant(),
                            ((Timestamp) basicTuple.get("updatedAt")).toInstant(),
                            (Integer) basicTuple.get("reviewRating"),
                            (String) basicTuple.get("reviewContent"),
                            // 추가된 상세 정보
                            completedLessIds,
                            lastCompletedLessonChapterId,
                            firstChapterId,
                            firstLessonId
                    );
                }).toList();

        return responses;
    }

    @Transactional(readOnly = true)
    public SelectEnrollmentResponse selectEnrollment(String userId, SelectEnrollmentRequest request) {

        // 요청을 보낸 유저와, 수강 신청한 유저 검증
        validUser(userId, request.enrollmentId());
        // Enrollment 검증
        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId()).orElseThrow(EnrollmentNotFoundException::new);

        // Native 쿼리
        Object result = enrollmentRepository.selectEnrollment(request.enrollmentId());
        Object[] row = (Object[]) result;

        Long lectureId = (Long) row[1];
        Long enrollmentId = (Long) row[0];
        Integer progress = (Integer) row[2];
        String completedLessonIdsString = (String) row[3];
        List<Long> completedLessIds = (completedLessonIdsString != null && !completedLessonIdsString.isEmpty())
                ? Arrays.stream(completedLessonIdsString.split(","))
                .map(Long::parseLong)
                .toList() : null;
        String lastCompletedLessonChapterIdsString = (String) row[4];
        Long lastCompletedLessonChapterId = (lastCompletedLessonChapterIdsString != null) ? Long.parseLong(lastCompletedLessonChapterIdsString) : null;
        Long firstLessonId = (row[5] != null) ? ((Number) row[5]).longValue() : null;
        Long firstChapterId = (row[6] != null) ? ((Number) row[6]).longValue() : null;

        return new SelectEnrollmentResponse(
                lectureId,
                enrollmentId,
                progress,
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
        Enrollment requestEnrollment = enrollmentRepository.findById(request.enrollmentId()).orElseThrow(EnrollmentNotFoundException::new);

        Object result = enrollmentRepository.getProgressCounts(requestEnrollment.getId());

        Object[] row = (Object[]) result;

        int completedLessonCount = ((Number) row[0]).intValue();
        int totalLessonCount = ((Number) row[1]).intValue();
        int updateProgress = (int) Math.round((double) completedLessonCount / totalLessonCount * 100);

        requestEnrollment.updateProgress(updateProgress);

        if (updateProgress == 100 && requestEnrollment.getStatus() == EnrollmentStatus.IN_PROGRESS) {
            requestEnrollment.updateStatus(EnrollmentStatus.COMPLETED);
        }
    }

    // 통계 업데이트 헬퍼 메서드
    // Lecture 생성 시 LectureStatistic이 함께 생성되므로 항상 존재해야 함
    // 낙관적 잠금 충돌 시 재시도 로직 포함
    private void updateEnrollmentCount(Long lectureId, boolean isAdd) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
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
                if (retryCount >= maxRetries) {
                    log.error("Failed to update LectureStatistic enrollment count after {} retries for lectureId: {}", 
                        maxRetries, lectureId, e);
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

    private List<Long> parseCommaSeparatedIds(String idsString) {
        if (idsString == null || idsString.trim().isEmpty()) {
            return List.of();
        }

        // **주의**: 쿼리에서 DISTINCT를 사용했더라도, MySQL의 GROUP_CONCAT은 문자열을 반환합니다.
        return Arrays.stream(idsString.split(","))
                .map(String::trim) // 공백 제거
                .filter(s -> !s.isEmpty()) // 빈 문자열 제거
                .map(s -> {
                    try {
                        return Long.parseLong(s);
                    } catch (NumberFormatException e) {
                        // 긴급 상황이므로 일단 예외 발생
                        throw new RuntimeException("ID 파싱 오류: " + s, e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * String 형태의 숫자 값을 Long으로 파싱합니다. (SUBSTRING_INDEX 결과 처리)
     */
    private Long parseLongOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            // 긴급 상황이므로 일단 예외 발생
            throw new RuntimeException("숫자 변환 오류: " + value, e);
        }
    }
}
