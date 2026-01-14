package com.teamexp.learnflowapi.content.dto;

public record UploadInitRequest(
        String filename,
        String contentType,
        Long filesize
) {
}
