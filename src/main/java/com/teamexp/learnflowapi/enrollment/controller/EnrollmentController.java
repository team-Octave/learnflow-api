package com.teamexp.learnflowapi.enrollment.controller;

import com.teamexp.learnflowapi.enrollment.dto.DecidedEnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.EnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.dto.EnrollmentResponse;
import com.teamexp.learnflowapi.enrollment.dto.GetEnrollmentRequest;
import com.teamexp.learnflowapi.enrollment.service.EnrollmentService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<BaseResponse<EnrollmentResponse>> create(@Valid @RequestBody EnrollmentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.ok(enrollmentService.createEnrollment(request)));
    }

//  TODO @Authorization userID 추출
    @GetMapping
    public ResponseEntity<BaseResponse<List<EnrollmentResponse>>> getEnrollments(@Valid @RequestBody GetEnrollmentRequest request) {

//      TODO return ResponseEntity.ok(BaseResponse.ok(enrollmentService.getEnrollments(request)))
        return ResponseEntity.ok().body(BaseResponse.ok(enrollmentService.getEnrollments(request)));
    }

    @PostMapping("/click")
    public ResponseEntity<Void> click(@Valid @RequestBody DecidedEnrollmentRequest request) {
        enrollmentService.updateEnrollment(request);
        return ResponseEntity.ok().build();
    }
}
