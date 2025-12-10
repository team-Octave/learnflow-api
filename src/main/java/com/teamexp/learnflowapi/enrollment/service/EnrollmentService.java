package com.teamexp.learnflowapi.enrollment.service;

import com.teamexp.learnflowapi.enrollment.dto.*;
import com.teamexp.learnflowapi.enrollment.exception.CompletedLessonAlreadyExistsException;
import com.teamexp.learnflowapi.enrollment.exception.EnrollmentAccessDeniedException;
import com.teamexp.learnflowapi.enrollment.exception.EnrollmentAlreadyExistsException;
import com.teamexp.learnflowapi.enrollment.exception.EnrollmentNotFoundException;
import com.teamexp.learnflowapi.enrollment.model.CompletedLesson;
import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.repository.CompletedLessonRepository;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Native 쿼리로 조회 후 바로 DTO로 매핑 후 반환
        return enrollmentRepository.findMyEnrollmentsByUserIdNative(userId);
    }

    public SelectEnrollmentResponse selectEnrollment(SelectEnrollmentRequest request) {

        // Native 쿼리로 결과 반환
        Object result = enrollmentRepository.selectEnrollment(request.enrollmentId());

        Object[] resultArray = (Object[]) result;

        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId()).orElseThrow(EnrollmentNotFoundException::new);

        enrollment.update();

        // DTO에 맞게 매핑
        return new SelectEnrollmentResponse(
                (Long) resultArray[1],
                (Long) resultArray[0],
                (Integer) resultArray[2],
                Arrays.stream(((String) resultArray[3]).split(",")).map(Long::parseLong).collect(Collectors.toList()),
                (Long) resultArray[4]
        );
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

        int completed = ((Number) result[0]).intValue();
        int total = ((Number) result[1]).intValue();

        int updateProgress = 0;

        if (total > 0) {
            updateProgress = (int) Math.round((double) completed / total * 100);
        }

        requestEnrollment.updateProgress(updateProgress);
    }

}
