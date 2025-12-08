package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.PresignedUrlResponse;
import com.teamexp.learnflowapi.content.model.Thumbnail;
import com.teamexp.learnflowapi.content.repository.ThumbnailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;

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

    @Transactional
    public void saveOrUpdateThumbnail(Long lectureId, String fileKey) {
        Thumbnail thumbnail = thumbnailRepository.findByLectureId(lectureId)
                .orElseGet(() -> new Thumbnail(lectureId, fileKey));

        thumbnail.changeFileKey(fileKey);
        thumbnailRepository.save(thumbnail);
    }

    @Transactional(readOnly = true)
    public Optional<Thumbnail> getThumbnail(Long lectureId) {
        return thumbnailRepository.findByLectureId(lectureId);
    }
}
