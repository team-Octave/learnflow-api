package com.teamexp.learnflowapi.content.controller;

import com.teamexp.learnflowapi.content.dto.UploadInitRequest;
import com.teamexp.learnflowapi.content.dto.UploadInitResponse;
import com.teamexp.learnflowapi.content.dto.UploadVideoRequest;
import com.teamexp.learnflowapi.content.service.ContentMediaService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
public class ContentMediaController {


    private final ContentMediaService contentMediaService;

    @PostMapping(value = "/upload-video-init",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<UploadInitResponse>> initUpload(
            @PathVariable Long lessonId,
            @RequestBody UploadInitRequest request
    ){
        UploadInitResponse data = contentMediaService.initUpload(lessonId, request);
        return ResponseEntity.ok(BaseResponse.ok(data));
    }


}

