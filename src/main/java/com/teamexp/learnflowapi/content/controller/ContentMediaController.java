package com.teamexp.learnflowapi.content.controller;

import com.teamexp.learnflowapi.content.dto.ContentUploadResponse;
import com.teamexp.learnflowapi.content.dto.UploadVideoRequest;
import com.teamexp.learnflowapi.content.service.ContentMediaService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
public class ContentMediaController {


    private final ContentMediaService contentMediaService;

    @PostMapping(value = "/upload-video",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ContentUploadResponse>> uploadVideo(
            @ModelAttribute UploadVideoRequest uploadVideoRequest
    ) throws IOException {

            Long mediaId = contentMediaService.requestVideoUpload(uploadVideoRequest);

            ContentUploadResponse data = new ContentUploadResponse(mediaId);

            return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(BaseResponse.ok(data));
    }

    @PostMapping(value = "/upload-video/retry",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ContentUploadResponse>> retryUploadVideo(
            @ModelAttribute UploadVideoRequest uploadVideoRequest
    ) throws IOException {

        Long mediaId = contentMediaService.retryVideoUpload(uploadVideoRequest.lessonId(), uploadVideoRequest.file());

        ContentUploadResponse data = new ContentUploadResponse(mediaId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(BaseResponse.ok(data));
    }

}

