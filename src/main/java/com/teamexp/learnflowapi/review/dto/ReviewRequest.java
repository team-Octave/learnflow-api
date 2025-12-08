package com.teamexp.learnflowapi.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
    @NotNull(message = "강의 ID는 필수입니다.")
    Long lectureId,

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점이어야 합니다.")
    @Max(value = 5, message = "평점은 최대 5점이어야 합니다.")
    Integer rating,

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    String content
) {
}
