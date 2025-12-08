package com.teamexp.learnflowapi.enrollment.controller;

import com.teamexp.learnflowapi.enrollment.dto.DecidedEnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.EnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.EnrollmentResponse;
import com.teamexp.learnflowapi.enrollment.service.EnrollmentService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/enrollment")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<BaseResponse<EnrollmentResponse>> createEnrollment(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                                             @Valid @RequestBody EnrollmentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse
                        .ok(enrollmentService.createEnrollment(principal.getId(), request)));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<EnrollmentResponse>>> getEnrollment(@AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(BaseResponse.ok(enrollmentService.getEnrollments(principal.getId())));
    }

    // TODO 테스트용
    @PostMapping("/select-enrollment")
    public ResponseEntity<Void> selectEnrollment(@Valid @RequestBody DecidedEnrollmentRequest request) {
        enrollmentService.updateEnrollment(request);
        return ResponseEntity.ok().build();
    }
}
