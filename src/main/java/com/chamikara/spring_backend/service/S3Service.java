package com.chamikara.spring_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class S3Service {

    private final S3Presigner s3Presigner;
    private final String bucketName;

    public S3Service(
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucketName}") String bucketName) {
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    /**
     * Generates a pre-signed PUT URL for direct client-to-S3 uploads.
     *
     * @param extension  File extension without the dot (e.g. "jpg", "png")
     * @param folderPath S3 folder prefix (e.g. "transformers/base")
     * @return A record containing the pre-signed uploadUrl (valid 15 min) and the objectKey to persist
     */
    public PresignedUpload generateUploadUrl(String extension, String folderPath) {
        String normalizedFolder = folderPath.endsWith("/") ? folderPath : folderPath + "/";
        String objectKey = normalizedFolder + UUID.randomUUID() + "." + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
        String uploadUrl = presigned.url().toString();

        log.info("Generated pre-signed upload URL for key: {}", objectKey);
        return new PresignedUpload(uploadUrl, objectKey);
    }

    /**
     * Generates a pre-signed GET URL for displaying a private S3 object.
     *
     * @param objectKey The S3 object key stored in the database
     * @return A pre-signed URL valid for 60 minutes
     */
    public String generateDownloadUrl(String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        String downloadUrl = presigned.url().toString();

        log.info("Generated pre-signed download URL for key: {}", objectKey);
        return downloadUrl;
    }

    public record PresignedUpload(String uploadUrl, String objectKey) {}
}
