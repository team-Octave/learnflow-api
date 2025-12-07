package com.teamexp.learnflowapi.review.controller;

import com.teamexp.learnflowapi.global.response.BaseResponse;
import com.teamexp.learnflowapi.review.dto.ReviewRequest;
import com.teamexp.learnflowapi.review.dto.ReviewResponse;
import com.teamexp.learnflowapi.review.service.ReviewService;
import com.teamexp.learnflowapi.user.config.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 1. 수강평 작성
    @PostMapping
    public BaseResponse<ReviewResponse> createReview(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @Valid @RequestBody ReviewRequest request
    ) {
        // User ID 타입(String) 그대로 사용 (파싱 제거)
        String userId = principal.getId();

        ReviewResponse response = reviewService.createReview(userId, request);
        return BaseResponse.ok(response);
    }

    // 2. 강의별 리뷰 조회 (공개 API)
    @GetMapping("/lectures/{lectureId}")
    public BaseResponse<Page<ReviewResponse>> getReviewsByLecture(
        @PathVariable Long lectureId,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReviewResponse> response = reviewService.getReviewsByLecture(lectureId, pageable);
        return BaseResponse.ok(response);
    }

    // 3. 내 리뷰 조회 (마이페이지)
    @GetMapping("/my")
    public BaseResponse<Page<ReviewResponse>> getMyReviews(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String userId = principal.getId();
        Page<ReviewResponse> response = reviewService.getMyReviews(userId, pageable);
        return BaseResponse.ok(response);
    }
}
