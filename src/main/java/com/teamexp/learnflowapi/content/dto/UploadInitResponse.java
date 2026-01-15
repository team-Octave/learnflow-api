package com.teamexp.learnflowapi.content.dto;

public record UploadInitResponse(
        Long mediaId,
        String  uploadUrl
) {
}
