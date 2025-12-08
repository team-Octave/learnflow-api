package com.teamexp.learnflowapi.enrollment.service;

import com.teamexp.learnflowapi.enrollment.dto.DecidedEnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.EnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.EnrollmentResponse;
import com.teamexp.learnflowapi.enrollment.dto.GetEnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
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

    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    // 수강 생성
    public EnrollmentResponse createEnrollment(EnrollmentRequest request) {

        // 수강이 되있는지 확인, exist로 리팩토링 예정
        if (enrollmentRepository.findByUserIdAndLectureId(request.userId(), request.lectureId()).isPresent()) {
//          TODO 예외 처리 수정 예정
            throw new IllegalStateException("Enrollment already exists");
        }

        Enrollment newEnrollment = enrollmentRepository.save(Enrollment.create(request.userId(), request.lectureId()));

//      TODO 생성된 Enrollment 확인(테스트), 반환 값 수정 예정
        return EnrollmentResponse.from(newEnrollment);
    }

    // 2. 현재 수강중인 강좌 목록 조회
    public List<EnrollmentResponse> getEnrollments(GetEnrollmentRequest request) {

        List<Enrollment> enrollments = enrollmentRepository.findByUserId(request.userId());

        return enrollments.stream().map(EnrollmentResponse::from).toList();
    }

    public void updateEnrollment(DecidedEnrollmentRequest request) {

        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(() -> new IllegalStateException("Enrollment not found"));

        enrollment.update();
    }

}
