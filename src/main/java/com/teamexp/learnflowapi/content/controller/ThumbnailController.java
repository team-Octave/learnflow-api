package com.teamexp.learnflowapi.content.controller;

import com.teamexp.learnflowapi.content.dto.PresignedUrlResponse;
import com.teamexp.learnflowapi.content.dto.UploadUrlRequest;
import com.teamexp.learnflowapi.content.service.ThumbnailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lectures/{lectureId}")
@RequiredArgsConstructor
public class ThumbnailController {

    private final ThumbnailService thumbnailService;

    /**
     * 썸네일 업로드용 Presigned URL 생성하기
     * */

    @PostMapping("/thumbnail/upload-url")
    public ResponseEntity<PresignedUrlResponse> createThumbnailUploadUrl(
            @PathVariable Long lectureId,@RequestBody UploadUrlRequest uploadUrlRequest
    ){
        PresignedUrlResponse response = thumbnailService.createUploadUrl(
                lectureId,
                uploadUrlRequest.fileName(),
                uploadUrlRequest.contentType()
        );
        return ResponseEntity.ok(response);
    }
}
