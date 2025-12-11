package com.teamexp.learnflowapi.content.controller;

import com.teamexp.learnflowapi.content.dto.ThumbnailUploadResponse;
import com.teamexp.learnflowapi.content.dto.UploadThumbnailRequest;
import com.teamexp.learnflowapi.content.service.ThumbnailService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.validation.Valid;
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
public class ThumbnailController {

    private final ThumbnailService thumbnailService;

    public ThumbnailController(ThumbnailService thumbnailService) {
        this.thumbnailService = thumbnailService;
    }

    @PostMapping(value = "/upload-thumbnail",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ThumbnailUploadResponse>> uploadThumbnail(
            @ModelAttribute @Valid UploadThumbnailRequest uploadThumbnailRequest) throws IOException {

            ThumbnailUploadResponse response = thumbnailService.uploadThumbnail(uploadThumbnailRequest);

            return ResponseEntity.ok(BaseResponse.ok(response));
    }
}
