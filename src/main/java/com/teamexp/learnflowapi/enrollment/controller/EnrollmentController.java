package com.teamexp.learnflowapi.enrollment.controller;

import com.teamexp.learnflowapi.enrollment.dto.CreateCompletedLessonRequest;
import com.teamexp.learnflowapi.enrollment.dto.DecidedEnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.EnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.EnrollmentResponse;
import com.teamexp.learnflowapi.enrollment.service.EnrollmentService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO Response 세분화 후 Refactor 적용 예정
@RestController
@RequestMapping("api/v1/enrollment")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<BaseResponse<EnrollmentResponse>> createEnrollment(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                                             @Valid @RequestBody EnrollmentRequest request) {

        // 생성 성공 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse
                        .ok(enrollmentService.createEnrollment(principal.getId(), request)));
    }

    @PostMapping("/complete-lesson")
    public ResponseEntity<Void> completeLesson(@AuthenticationPrincipal CustomUserPrincipal principal,
                                               @Valid @RequestBody CreateCompletedLessonRequest request) {
        enrollmentService.createCompletedLesson(principal.getId() , request);
        // 완료한 lessonId, enrollmentId?
        return ResponseEntity.ok().build();
    }

    @PostMapping("/select-enrollment")
    public ResponseEntity<Void> selectEnrollment(@Valid @RequestBody DecidedEnrollmentRequest request) {
        enrollmentService.updateEnrollment(request);
        // lecture정보 > 챕터 > 레슨(완료 true만), 가장 마지막에 완료된 레슨 ID, 진도율
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<EnrollmentResponse>>> getEnrollment(@AuthenticationPrincipal CustomUserPrincipal principal) {

        // lecture 썸네일, lecture 이름, 수강률, review 별점, review수강평, 생성일, 업데이트일, Status 반환
        return ResponseEntity.ok(BaseResponse.ok(enrollmentService.getEnrollments(principal.getId())));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteEnrollment(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                 @Valid @RequestBody DecidedEnrollmentRequest request) {
        enrollmentService.deleteEnrollment(principal.getId() , request);
        return ResponseEntity.ok().build();
    }
}
