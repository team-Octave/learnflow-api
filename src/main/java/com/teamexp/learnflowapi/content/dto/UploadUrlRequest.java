package com.teamexp.learnflowapi.content.dto;

public record UploadUrlRequest(
        String fileName,
        String contentType
) {
}
