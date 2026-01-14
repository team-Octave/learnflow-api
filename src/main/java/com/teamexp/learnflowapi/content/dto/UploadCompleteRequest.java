package com.teamexp.learnflowapi.content.dto;

public record UploadCompleteRequest(
        Long mediaId,
        String fileKey,
        String filename,
        Long filesize,
        String contentType,
        Integer durationSec
) {
}
