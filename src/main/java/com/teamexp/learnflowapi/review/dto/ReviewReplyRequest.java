package com.teamexp.learnflowapi.review.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewReplyRequest(
    @NotBlank(message = "답글 내용은 필수입니다.")
    String replyContent
    ) {
}
