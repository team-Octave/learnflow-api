package com.teamexp.learnflowapi.content.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3PresignedUrlService {

    private final S3Presigner presigner;
    private final String bucketName = "my-lxp-videos";

    public URL createUploadUrl(String key, Duration expiresIn, String contentType) {

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(objectRequest)
                .signatureDuration(expiresIn)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);

        return presigned.url();
    }
}
