package com.teamexp.learnflowapi.content.controller;

import com.teamexp.learnflowapi.content.dto.PresignedUrlResponse;
import com.teamexp.learnflowapi.content.dto.UploadUrlRequest;
import com.teamexp.learnflowapi.content.dto.VideoUpdateRequest;
import com.teamexp.learnflowapi.content.service.ContentMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lessons/{lessonId}/")
@RequiredArgsConstructor
public class ContentMediaController {

    private final ContentMediaService contentMediaService;

    /**
     * 영상 업로드용 Presigned URL 생성하기
     */

    @PostMapping("/video/upload-url")
    public ResponseEntity<PresignedUrlResponse> createVideoUploadUrl(
            @PathVariable Long lessonId,
            @RequestBody UploadUrlRequest uploadUrlRequest) {

        PresignedUrlResponse response = contentMediaService.createVideoUploadUrl(
                lessonId,
                uploadUrlRequest.fileName(),
                uploadUrlRequest.contentType()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 영상 등록 및 수정하기
     */
    @PatchMapping("/video")
    public ResponseEntity<Void> saveOrUpdateVideo(
            @PathVariable Long lessonId,
            @RequestBody VideoUpdateRequest request
    ) {
        contentMediaService.saveOrUpdateVideo(
                lessonId,
                request.fileKey(),
                request.durationSec()
        );
        return ResponseEntity.noContent().build();
    }

}
