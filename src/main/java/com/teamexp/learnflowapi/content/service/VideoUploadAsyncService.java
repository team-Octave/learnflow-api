package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.external.GcpFileUploadService;
import com.teamexp.learnflowapi.content.model.ContentMedia;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;
import lombok.RequiredArgsConstructor;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class VideoUploadAsyncService {

    private final ContentMediaRepository contentMediaRepository;
    private final GcpFileUploadService gcpFileUploadService;
    private static final Logger log = LoggerFactory.getLogger(VideoUploadAsyncService.class);

    @Async("videoUploadExecutor")
    @Transactional
    public void uploadVideoFileAsync(Long contentMediaId, File localFile) {

        // db에서 ContentMedia 조회
        ContentMedia media = contentMediaRepository.findById(contentMediaId).orElseThrow(() ->
                new IllegalArgumentException("ContentMedia를 찾을 수 없습니다. ID: " + contentMediaId));

        final int maxRetryAttempts = 3;

        try {
            int attempt = 1;
            while (true) {
                try {
                    // FFmpegFrameGrabber으로 duration 계산
                    Integer durationSec = getDurationSeconds(localFile);

                    // GCS 업로드
                    String fileKey = gcpFileUploadService.uploadFile(localFile, "videos/");

                    // ContentMedia 업데이트(fileKey, duration)
                    media.completeUpload(fileKey, durationSec);
                    return;
                } catch (Exception e) {
                    if (attempt >= maxRetryAttempts) {
                        media.failUpload();
                        log.error("[Async-VideoUpload] 업로드 완전 실패 - mediaId={}, attempts={}, error={}",
                                contentMediaId, attempt, e.getMessage(), e);
                        return;
                    } else {
                        log.warn("[Async-VideoUpload] 업로드 실패, 재시도 예정 - mediaId={}, attempt={}, error={}",
                                contentMediaId, attempt, e.getMessage());
                        attempt++;

                        try {
                            Thread.sleep(1000L * attempt); // 간단한 backoff (1초, 2초, 3초…)
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            media.failUpload();
                            log.error("[Async-VideoUpload] 재시도 대기 중 - mediaId={}", contentMediaId, ie);
                            return;
                        }
                    }
                }
            }
        } finally {
            if (localFile != null && localFile.exists()) {
                if (!localFile.delete()) {
                    log.warn("임시 파일 삭제 실패: {}", localFile.getAbsolutePath());
                }
            }
        }
    }

    private Integer getDurationSeconds(File file) throws IOException {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
            grabber.start();
            long durationMicro = grabber.getLengthInTime(); // us 단위
            grabber.stop();
            return (int) (durationMicro / 1_000_000L);
        }
    }
}
