package com.teamexp.learnflowapi.content.controller;

import com.teamexp.learnflowapi.content.dto.UploadCompleteRequest;
import com.teamexp.learnflowapi.content.dto.UploadCompleteResponse;
import com.teamexp.learnflowapi.content.dto.UploadInitRequest;
import com.teamexp.learnflowapi.content.dto.UploadInitResponse;
import com.teamexp.learnflowapi.content.dto.UploadVideoRequest;
import com.teamexp.learnflowapi.content.service.ContentMediaService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.validation.Valid;
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

    // 영상 업로드에 필요한 signed url 전달
    @PostMapping(value = "/upload-video-init",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse<UploadInitResponse>> initUpload(
            @Valid @RequestBody UploadInitRequest request
    ){
        UploadInitResponse data = contentMediaService.initUpload(request);
        return ResponseEntity.ok(BaseResponse.ok(data));
    }


    // 영상 업로드 성공 반환
    @PostMapping(value = "/upload-video-completed")
    public ResponseEntity<BaseResponse<UploadCompleteResponse>> completeUpload(
            @Valid @RequestBody UploadCompleteRequest request
    ){

        contentMediaService.completeUpload(request);
        UploadCompleteResponse data = new UploadCompleteResponse(true);
        return ResponseEntity.ok(BaseResponse.ok(data));
    }
}
