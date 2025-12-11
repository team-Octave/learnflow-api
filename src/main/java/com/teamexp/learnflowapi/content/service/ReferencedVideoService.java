package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.VideoUrlRequest;
import com.teamexp.learnflowapi.content.dto.VideoUrlResponse;
import com.teamexp.learnflowapi.content.excption.RequestEmptyException;
import com.teamexp.learnflowapi.content.excption.VideoUrlEmptyException;
import com.teamexp.learnflowapi.content.model.ReferencedVideo;
import com.teamexp.learnflowapi.content.repository.ReferencedVideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ReferencedVideoService {

    private final ReferencedVideoRepository referencedVideoRepository;

    public  ReferencedVideoService(ReferencedVideoRepository referencedVideoRepository) {
        this.referencedVideoRepository = referencedVideoRepository;

    }

    @Transactional
    public VideoUrlResponse  createReferencedVideoUrl(VideoUrlRequest request) {

        if (request == null) {
            throw new RequestEmptyException();
        }

        Long lessonId = request.lessonId();
        String videoUrl = request.videoUrl();

        if (lessonId == null) {
            throw new IllegalArgumentException("lessonId가 비어있습니다.");
        }
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new VideoUrlEmptyException();
        }
        ReferencedVideo foundVideo = referencedVideoRepository.findByLessonId(lessonId)
                .orElse(null);

        if (foundVideo != null) {
            foundVideo.changeVideoUrl(videoUrl);
        } else {
            foundVideo = ReferencedVideo.createReferencedVideo(lessonId, videoUrl);
        }

        ReferencedVideo saved = referencedVideoRepository.save(foundVideo);

        return new VideoUrlResponse(saved.getLessonId(),saved.getVideoUrl());
    }
}
