package com.teamexp.learnflowapi.enrollment.service;

import com.teamexp.learnflowapi.enrollment.dto.CreateCompletedLessonRequest;
import com.teamexp.learnflowapi.enrollment.dto.DecidedEnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.EnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.EnrollmentResponse;
import com.teamexp.learnflowapi.enrollment.exception.EnrollmentAccessDeniedException;
import com.teamexp.learnflowapi.enrollment.exception.EnrollmentAlreadyExistsException;
import com.teamexp.learnflowapi.enrollment.exception.EnrollmentNotFoundException;
import com.teamexp.learnflowapi.enrollment.model.CompletedLesson;
import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.repository.CompletedLessonRepository;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public EnrollmentResponse createEnrollment(String userId , EnrollmentRequest request) {

        if (enrollmentRepository.existsByUserIdAndLectureId(userId, request.lectureId())) {
            throw new EnrollmentAlreadyExistsException();
        }

        Enrollment newEnrollment = enrollmentRepository.save(Enrollment.create(userId, request.lectureId()));

//      TODO 생성된 Enrollment 확인(테스트), 반환 값 협의 후 수정 예정
        return EnrollmentResponse.from(newEnrollment);
    }

    // 5. lesson 완료
    public void createCompletedLesson(String userId , CreateCompletedLessonRequest request) {

        if (!enrollmentRepository.existsById(request.enrollmentId())) {
            throw new EnrollmentNotFoundException();
        }

        validUser(userId, request.enrollmentId());

        CompletedLesson completedLesson = CompletedLesson
                .createCompletedLesson(request.enrollmentId(), request.lessonId());

        completedLessonRepository.save(completedLesson);
    }

    // 2. 현재 수강중인 강좌 목록 조회
    public List<EnrollmentResponse> getEnrollments(String userId) {

        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);

        return enrollments.stream().map(EnrollmentResponse::from).toList();
    }

    public void updateEnrollment(DecidedEnrollmentRequest request) {

        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(EnrollmentNotFoundException::new);

        enrollment.update();
    }

    private void validUser(String userId, Long enrollmentId) {

        Enrollment requestEnrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(EnrollmentNotFoundException::new);
        if (!requestEnrollment.getUserId().equals(userId)) {
            throw new EnrollmentAccessDeniedException();
        }
    }

}
