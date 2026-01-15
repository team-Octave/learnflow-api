package com.teamexp.learnflowapi.content.dto;

import jakarta.validation.constraints.NotBlank;

public record UploadInitRequest(
        @NotBlank String filename
) {
}
