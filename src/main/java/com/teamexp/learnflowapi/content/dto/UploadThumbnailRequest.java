package com.teamexp.learnflowapi.content.dto;

import org.springframework.web.multipart.MultipartFile;

public record UploadThumbnailRequest(
        MultipartFile file,
        Long lectureId
) {
}
