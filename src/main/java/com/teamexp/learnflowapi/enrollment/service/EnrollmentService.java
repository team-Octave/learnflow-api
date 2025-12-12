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
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatistic;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureStatisticRepository;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class EnrollmentService {

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

        List<Tuple> result = enrollmentRepository.findMyEnrollmentsByUserIdNative(userId);

        List<MyEnrollmentResponse> responses = result.stream()
                .map(tuple -> new MyEnrollmentResponse(
                        (Long) tuple.get("lectureId"),
                        (Long) tuple.get("enrollmentId"),
                        tuple.get("reviewId") != null ? (Long) tuple.get("reviewId") : null,
                        (String) tuple.get("lectureThumbnail"),
                        (String) tuple.get("lectureTitle"),
                        EnrollmentStatus.valueOf((String) tuple.get("enrollmentStatus")),
                        (Integer) tuple.get("progress"),
                        ((Timestamp) tuple.get("enrolledAt")).toInstant(),
                        ((Timestamp) tuple.get("updatedAt")).toInstant(),
                        (Integer) tuple.get("reviewRating"),
                        (String) tuple.get("reviewContent")
                )).toList();
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

        Long lectureId = (Long) row[0];
        Long enrollmentId = (Long) row[1];
        Integer progress = (Integer) row[2];
        String completedLessonIdsString = (String) row[3];
        List<Long> completedLessIds = (completedLessonIdsString != null && !completedLessonIdsString.isEmpty())
                ? Arrays.stream(completedLessonIdsString.split(","))
                .map(Long::parseLong)
                .toList() : null;
        Long lastCompletedLessonChapterId = (row[4] != null) ? ((Number) row[4]).longValue() : null;

        return new SelectEnrollmentResponse(
                lectureId,
                enrollmentId,
                progress,
                completedLessIds,
                lastCompletedLessonChapterId
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
    private void updateEnrollmentCount(Long lectureId, boolean isAdd) {
        LectureStatistic statistic = lectureStatisticRepository.findById(lectureId)
            .orElseThrow(LectureNotFoundException::new);

        // 통계 업데이트
        if (isAdd) {
            statistic.addEnrollment();
        } else {
            statistic.removeEnrollment();
        }

        lectureStatisticRepository.save(statistic);
    }
}
