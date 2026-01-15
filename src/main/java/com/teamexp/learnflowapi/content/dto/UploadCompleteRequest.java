package com.teamexp.learnflowapi.content.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UploadCompleteRequest(
        @NotNull Long mediaId,
        @NotNull @Positive
        Integer durationSec
) {
}
