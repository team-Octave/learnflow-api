package com.teamexp.learnflowapi.content.dto;

public record UploadCompleteRequest(
        Long mediaId,
        Integer durationSec
) {
}
