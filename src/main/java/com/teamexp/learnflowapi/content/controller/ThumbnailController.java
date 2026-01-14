package com.teamexp.learnflowapi.content.controller;

import com.teamexp.learnflowapi.content.dto.ThumbnailResponse;
import com.teamexp.learnflowapi.content.service.ThumbnailService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<BaseResponse<ThumbnailResponse>> uploadThumbnail(
            @RequestParam("file") MultipartFile file) throws IOException {

        ThumbnailResponse data = thumbnailService.uploadThumbnail(file);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok(data));
    }
}
