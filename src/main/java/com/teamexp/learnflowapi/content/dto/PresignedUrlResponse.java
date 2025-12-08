package com.teamexp.learnflowapi.content.dto;

public record PresignedUrlResponse(
        String uploadUrl,
        String fileKey
) {}

