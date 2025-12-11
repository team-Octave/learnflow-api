package com.teamexp.learnflowapi.enrollment.service;

import com.teamexp.learnflowapi.enrollment.dto.*;
import com.teamexp.learnflowapi.enrollment.exception.CompletedLessonAlreadyExistsException;
import com.teamexp.learnflowapi.enrollment.exception.EnrollmentAccessDeniedException;
import com.teamexp.learnflowapi.enrollment.exception.EnrollmentAlreadyExistsException;
import com.teamexp.learnflowapi.enrollment.exception.EnrollmentNotFoundException;
import com.teamexp.learnflowapi.enrollment.model.CompletedLesson;
import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.model.EnrollmentStatus;
import com.teamexp.learnflowapi.enrollment.repository.CompletedLessonRepository;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 1. 수강 생성
 * 2. 나의 수강 조회 -> 완강도 포함?
 * 3. 수강 삭제 -> 물리 삭제
 * 4. lesson 클릭 시 업데이트
 * 5. 완료 된 수강 추가
 * 6. 수강률 계산
 */
@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CompletedLessonRepository completedLessonRepository;

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CompletedLessonRepository completedLessonRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.completedLessonRepository = completedLessonRepository;
    }

    // 1. 수강 생성
    public void createEnrollment(String userId , CreateEnrollmentRequest request) {

        // 생성된 수강 확인
        if (enrollmentRepository.existsByUserIdAndLectureId(userId, request.lectureId())) {
            throw new EnrollmentAlreadyExistsException();
        }

        enrollmentRepository.save(Enrollment.create(userId, request.lectureId()));
    }

    // 5. lesson 완료
    public void createCompletedLesson(String userId , CreateCompletedLessonRequest request) {

        // 요청하는 enrollmentId 검증
        if (!enrollmentRepository.existsById(request.enrollmentId())) {
            throw new EnrollmentNotFoundException();
        }

        // enrollment에 있는 userId와 현재 로그인 userId 검증
        validUser(userId, request.enrollmentId());

        // completed lesson 중복 확인
        if (completedLessonRepository.existsByEnrollmentIdAndLessonId(request.enrollmentId(), request.lessonId())) throw new CompletedLessonAlreadyExistsException();

        CompletedLesson completedLesson = CompletedLesson
                .createCompletedLesson(request.enrollmentId(), request.lessonId());

        // completedLesson 생성과 진행률 계산
        completedLessonRepository.save(completedLesson);
        updateProgress(request);
    }

    // 2. 현재 수강중인 강좌 목록 조회
    @Transactional(readOnly = true)
    public List<MyEnrollmentResponse> getEnrollments(String userId) {

        List<Tuple> result = enrollmentRepository.findMyEnrollmentsByUserIdNative(userId);

        List<MyEnrollmentResponse> responses = result.stream()
                .map(tuple -> new MyEnrollmentResponse(
                        (Long) tuple.get("lectureId"),
                        (Long) tuple.get("enrollmentId"),
                        tuple.get("reviewId") != null ? (Long) tuple.get("reviewId") : null,  // null 처리
                        (String) tuple.get("lectureThumbnail"),
                        (String) tuple.get("lectureTitle"),
                        EnrollmentStatus.valueOf((String) tuple.get("enrollmentStatus")),  // Enum 변환
                        (Integer) tuple.get("progress"),
                        ((Timestamp) tuple.get("enrolledAt")).toInstant(),  // Instant 변환
                        ((Timestamp) tuple.get("updatedAt")).toInstant(),  // Instant 변환
                        (Integer) tuple.get("reviewRating"),
                        (String) tuple.get("reviewContent")
                )).toList();
        return responses;
    }

    public SelectEnrollmentResponse selectEnrollment(String userId, SelectEnrollmentRequest request) {

        validUser(userId, request.enrollmentId());

        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId()).orElseThrow(EnrollmentNotFoundException::new);

        Object result = enrollmentRepository.selectEnrollment(request.enrollmentId());
        enrollment.update();

        if (result instanceof Object[]) {
            Object[] resultArray = (Object[]) result;

            // 1. 기본 필드: Number 타입 안전하게 변환
            // Number로 캐스팅하며 Long, Integer 값을 안전하게 처리
            Long lectureId = ((Number) resultArray[0]).longValue(); // Index 0
            Long enrollmentId = ((Number) resultArray[1]).longValue(); // Index 1
            Integer progress = ((Number) resultArray[2]).intValue(); // Index 2

            // 2. Index 3 (completedLessonIds): String으로 안전하게 변환 후 List<Long>으로 처리
            String completedLessonIdsString;
            if (resultArray[4] instanceof String) {
                completedLessonIdsString = (String) resultArray[4];
            } else {
                // String이 아닌 경우 (예: 이전 질문처럼 Long인 경우), String.valueOf()로 변환
                completedLessonIdsString = String.valueOf(resultArray[4]);
            }

            List<Long> completedLessonIds = Arrays.stream(completedLessonIdsString.split(","))
                    .filter(s -> !s.trim().isEmpty()) // 빈 문자열 필터링
                    .map(String::trim)
                    .map(s -> {
                        try {
                            return Long.valueOf(s);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 3. Index 4 (lastCompletedLessonChapterId): ClassCastException 처리 로직 적용
            Long lastCompletedLessonChapterId;
            if (resultArray[5] instanceof String) {
                // 현재 오류 발생 지점: String을 Long으로 변환
                String chapterIdString = ((String) resultArray[5]).trim();
                try {
                    lastCompletedLessonChapterId = Long.valueOf(chapterIdString);
                } catch (NumberFormatException e) {
                    lastCompletedLessonChapterId = 0L; // 변환 실패 시 기본값 처리
                }
            } else if (resultArray[5] instanceof Number) {
                // Number인 경우의 원래 로직 유지
                lastCompletedLessonChapterId = ((Number) resultArray[5]).longValue();
            } else {
                // 예상치 못한 타입 처리
                throw new IllegalArgumentException("lastCompletedLessonChapterId의 예상치 못한 타입: " + resultArray[5].getClass().getName());
            }

            // 4. 응답 객체 생성
            return new SelectEnrollmentResponse(
                    enrollmentId,
                    lectureId,
                    progress,
                    completedLessonIds,
                    lastCompletedLessonChapterId
            );

        } else {
            throw new IllegalArgumentException("Unsupported result type: " + result.getClass().getName());
        }
    }

    // 수강 삭제 (물리 삭제)
    public void deleteEnrollment(String userId, SelectEnrollmentRequest request) {

        validUser(userId, request.enrollmentId());

        try {
            enrollmentRepository.deleteById(request.enrollmentId());
        } catch (EmptyResultDataAccessException e) {
            throw new EnrollmentNotFoundException();
        }
    }

    private void validUser(String userId, Long enrollmentId) {

        Enrollment requestEnrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(EnrollmentNotFoundException::new);

        if (!Objects.equals(requestEnrollment.getUserId(), userId)) {
            throw new EnrollmentAccessDeniedException();
        }

    }

    private void updateProgress(CreateCompletedLessonRequest request) {
        Enrollment requestEnrollment = enrollmentRepository.findById(request.enrollmentId()).orElseThrow(EnrollmentNotFoundException::new);

        Object[] result = enrollmentRepository.getProgressCounts(request.enrollmentId());

        if (result != null && result.length >= 2) {
            int completed = extractInt(result[0]);
            int total = extractInt(result[1]);

            int updateProgress = 0;

            if (total > 0) {
                updateProgress = (int) Math.round((double) completed / total * 100);
            }
            requestEnrollment.updateProgress(updateProgress);
        } else  {
            requestEnrollment.updateProgress(0);
        }

    }

    private int extractInt(Object obj) {
        // Object가 Number 타입인지 확인하고, 아니라면 0을 반환
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        // 만약 obj가 Number가 아니면 0을 반환 (그 외의 값 처리)
        return 0;
    }

}
