package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.ThumbnailUploadResponse;
import com.teamexp.learnflowapi.content.dto.UploadThumbnailRequest;
import com.teamexp.learnflowapi.content.excption.InvalidFileNameException;
import com.teamexp.learnflowapi.content.excption.ThumbnailFileSizeExceededException;
import com.teamexp.learnflowapi.content.excption.ThumbnailUnsupportedExtensionException;
import com.teamexp.learnflowapi.content.excption.ThumbnailUnsupportedMineTypeException;
import com.teamexp.learnflowapi.content.excption.UploadNotExistException;
import com.teamexp.learnflowapi.content.external.GcpFileUploadService;
import com.teamexp.learnflowapi.content.model.Thumbnail;
import com.teamexp.learnflowapi.content.repository.ThumbnailRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;


@Service
public class ThumbnailService {

    private final ThumbnailRepository thumbnailRepository;
    private final GcpFileUploadService gcpFileUploadService;

    public ThumbnailService(ThumbnailRepository thumbnailRepository,
                            GcpFileUploadService gcpFileUploadService) {
        this.thumbnailRepository = thumbnailRepository;
        this.gcpFileUploadService = gcpFileUploadService;
    }

    private static final Set<String> ALLOWED_EXTENSIONS =  Set.of("jpg", "jpeg");;
    private static final Set<String> ALLOWED_MIMES = Set.of("image/jpeg", "image/jpg");
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;


    public ThumbnailUploadResponse uploadThumbnail(UploadThumbnailRequest request) throws IOException {

        MultipartFile file = request.file();
        Long lectureId = request.lectureId();

        if (file == null || file.isEmpty()) {
            throw new UploadNotExistException();
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new InvalidFileNameException();
        }

        String extension = originalFilename
                .substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ThumbnailUnsupportedExtensionException();
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIMES.contains(contentType)) {
            throw new ThumbnailUnsupportedMineTypeException();
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ThumbnailFileSizeExceededException();
        }

        // GCP 업로드용 파일명 생성
        String thumbnailFileName = "thumbnails/lecture-" + lectureId + "-" + java.util.UUID.randomUUID() + "." + extension;

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
        return new ThumbnailUploadResponse(lectureId, publicUrl);
    }
}
