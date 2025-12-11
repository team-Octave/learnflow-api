package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.ThumbnailResponse;
import com.teamexp.learnflowapi.content.dto.ThumbnailRequest;
import com.teamexp.learnflowapi.content.exception.InvalidFileNameException;
import com.teamexp.learnflowapi.content.exception.LectureThumbnailNotFoundException;
import com.teamexp.learnflowapi.content.exception.ThumbnailFileSizeExceededException;
import com.teamexp.learnflowapi.content.exception.ThumbnailUnsupportedExtensionException;
import com.teamexp.learnflowapi.content.exception.ThumbnailUnsupportedMimeTypeException;
import com.teamexp.learnflowapi.content.exception.UploadNotExistException;
import com.teamexp.learnflowapi.content.external.GcpFileUploadService;
import com.teamexp.learnflowapi.content.model.Thumbnail;
import com.teamexp.learnflowapi.content.repository.ThumbnailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


@Service
public class ThumbnailService {

    private final ThumbnailRepository thumbnailRepository;
    private final GcpFileUploadService gcpFileUploadService;

    public ThumbnailService(ThumbnailRepository thumbnailRepository,
                            GcpFileUploadService gcpFileUploadService) {
        this.thumbnailRepository = thumbnailRepository;
        this.gcpFileUploadService = gcpFileUploadService;
    }

    private static final Set<String> ALLOWED_EXTENSIONS =  Set.of("jpg", "jpeg");
    private static final Set<String> ALLOWED_MIMES = Set.of("image/jpeg", "image/jpg");
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;


    @Transactional
    public ThumbnailResponse uploadThumbnail(ThumbnailRequest request) throws IOException {

        MultipartFile file = request.file();
        Long lectureId = request.lectureId();

        // 업로드 파일 검증
        validateThumbnailFile(file);

        // GCP 업로드용 파일명 생성
        String originalFilename = file.getOriginalFilename();

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        String thumbnailFileName = "thumbnails/lecture-" + lectureId + "-" + UUID.randomUUID() + "." + extension;

        // GCP 업로드 수행
        gcpFileUploadService.uploadFile(thumbnailFileName, file);

        // 공개 URL 생성
        String publicUrl = gcpFileUploadService.createPublicUrl(thumbnailFileName);

        // DB 저장
        Thumbnail foundthumbnail = thumbnailRepository.findByLectureId(lectureId)
                .orElse(null);

        if (foundthumbnail != null) {
            // 기존 썸네일 있으면 -> 파일키 업데이트
            foundthumbnail.changeFileKey(thumbnailFileName, publicUrl);
        } else {
            // 없으면 신규 썸네일 저장
            Thumbnail created = Thumbnail.createThumbnail(
                    lectureId, thumbnailFileName, publicUrl
            );
            thumbnailRepository.save(created);
        }
        return new ThumbnailResponse(lectureId, publicUrl);
    }

    private void validateThumbnailFile(MultipartFile file){

        if (file == null || file.isEmpty()) {
            throw new UploadNotExistException();
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new InvalidFileNameException();
        }

        String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ThumbnailUnsupportedExtensionException();
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIMES.contains(contentType)) {
            throw new ThumbnailUnsupportedMimeTypeException();
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ThumbnailFileSizeExceededException();
        }
    }

    @Transactional(readOnly = true)
    public ThumbnailResponse getThumbnail(Long lectureId) {
        if (lectureId == null) {
            throw new IllegalArgumentException("lectureId는 null일 수 없습니다.");
        }
        Thumbnail thumbnail = thumbnailRepository.findByLectureId(lectureId)
                .orElseThrow(LectureThumbnailNotFoundException::new);

        return new ThumbnailResponse(thumbnail.getLectureId(), thumbnail.getFileUrl());
    }
}
