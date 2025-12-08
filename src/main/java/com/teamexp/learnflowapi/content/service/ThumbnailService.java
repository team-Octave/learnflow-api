package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.PresignedUrlResponse;
import com.teamexp.learnflowapi.content.repository.ThumbnailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ThumbnailService {

    private final ThumbnailRepository thumbnailRepository;
    private final S3PresignedUrlService s3PresignedUrlService;

    public PresignedUrlResponse createUploadUrl(Long lectureId, String fileName, String contentType) {
        String key = "thumbnails/lecture-" + lectureId + "-" + fileName;

        URL url = s3PresignedUrlService.createUploadUrl(
                key,
                Duration.ofMinutes(10),
                contentType
        );

        return new PresignedUrlResponse(url.toString(), key);
    }
}
