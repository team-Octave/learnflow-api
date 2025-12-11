package com.teamexp.learnflowapi.content.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record UploadThumbnailRequest(
        @NotNull(message = "파일은 필수입니다.")
        MultipartFile file,
        @NotNull(message = "Lecture ID는 필수입니다.")
        Long lectureId
) {
}
