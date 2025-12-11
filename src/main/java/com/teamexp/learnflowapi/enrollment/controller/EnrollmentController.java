package com.teamexp.learnflowapi.enrollment.controller;

import com.teamexp.learnflowapi.enrollment.dto.*;
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
    public ResponseEntity<BaseResponse<Void>> createEnrollment(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                                             @Valid @RequestBody CreateEnrollmentRequest request) {

        enrollmentService.createEnrollment(principal.getId(), request);
        // 생성 성공 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(null));
    }

    @PostMapping("/complete-lesson")
    public ResponseEntity<BaseResponse<Void>> completeLesson(@AuthenticationPrincipal CustomUserPrincipal principal,
                                               @Valid @RequestBody CreateCompletedLessonRequest request) {
        enrollmentService.createCompletedLesson(principal.getId() , request);
        // 완료한 lessonId, enrollmentId
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.ok(null));
    }

    @PostMapping("/select-enrollment")
    public ResponseEntity<BaseResponse<SelectEnrollmentResponse>> selectEnrollment(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                                                   @Valid @RequestBody SelectEnrollmentRequest request) {
        // lecture정보 > 챕터 > 레슨(완료 true만), 가장 마지막에 완료된 레슨 ID, 진도율
        return ResponseEntity.ok(BaseResponse.ok(enrollmentService.selectEnrollment(principal.getId() , request)));
    }

    @GetMapping("/my")
    public ResponseEntity<BaseResponse<List<MyEnrollmentResponse>>> getEnrollment(@AuthenticationPrincipal CustomUserPrincipal principal) {

        // lecture 썸네일, lecture 이름, 수강률, review 별점, review수강평, 생성일, 업데이트일, Status 반환
        return ResponseEntity.ok(BaseResponse.ok(enrollmentService.getEnrollments(principal.getId())));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponse<Void>> deleteEnrollment(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                 @Valid @RequestBody SelectEnrollmentRequest request) {
        enrollmentService.deleteEnrollment(principal.getId() , request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
