package com.teamexp.learnflowapi.content.dto;

import org.springframework.web.multipart.MultipartFile;

public record UploadVideoRequest(
        MultipartFile file,
        Long lessonId
) {
}
