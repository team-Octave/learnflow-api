package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.PresignedUrlResponse;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ContentMediaService {

    private final ContentMediaRepository contentMediaRepository;
    private final S3PresignedUrlService s3PresignedUrlService;

    public PresignedUrlResponse createVideoUploadUrl(Long lessonId, String fileName, String contentType) {
        String key = "videos/lesson-" + lessonId + "-" + fileName;

        URL url = s3PresignedUrlService.createUploadUrl(
                key,
                Duration.ofMinutes(30),
                contentType
        );

        return new PresignedUrlResponse(url.toString(), key);
    }
}
