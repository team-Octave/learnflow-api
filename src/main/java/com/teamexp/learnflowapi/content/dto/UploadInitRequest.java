package com.teamexp.learnflowapi.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadInitRequest(
        @NotBlank String filename,
        @NotBlank String contentType,
        @NotNull Long filesize
) {
}
