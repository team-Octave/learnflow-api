package com.teamexp.learnflowapi.content.dto;

public record VideoUpdateRequest(
        String fileKey,
        Integer durationSec
) {
}
