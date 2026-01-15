package com.teamexp.learnflowapi.content.controller;

import com.teamexp.learnflowapi.content.dto.VideoUrlRequest;
import com.teamexp.learnflowapi.content.dto.VideoUrlResponse;
import com.teamexp.learnflowapi.content.service.ReferencedVideoService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/contents")

public class ReferencedVideoController {

    private final ReferencedVideoService referencedVideoService;

    public ReferencedVideoController(ReferencedVideoService referencedVideoService) {
        this.referencedVideoService = referencedVideoService;
    }

    /**
     * 강의 영상 url 등록 / 수정
     *
     */
    @PostMapping("/upload-url")
    public ResponseEntity<BaseResponse<VideoUrlResponse>> saveVideoUrl(
            @RequestBody @Valid VideoUrlRequest request
    ) {
        VideoUrlResponse response = referencedVideoService.createReferencedVideoUrl(request);
        return ResponseEntity.ok(BaseResponse.ok(response));
    }
}
