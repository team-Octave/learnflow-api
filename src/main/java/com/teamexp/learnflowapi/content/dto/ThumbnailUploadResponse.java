package com.teamexp.learnflowapi.content.dto;

public record ThumbnailUploadResponse(
        Long lectureId,
        String uploadUrl
) {
}
