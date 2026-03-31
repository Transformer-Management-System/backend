package com.chamikara.spring_backend.controller;

import com.chamikara.spring_backend.dto.response.ApiResponse;
import com.chamikara.spring_backend.dto.response.PresignedUploadResponse;
import com.chamikara.spring_backend.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final S3Service s3Service;

    /**
     * Generates a pre-signed PUT URL for uploading an image directly from the client to S3.
     *
     * GET /api/v1/images/generate-upload-url?folder=transformers/base&extension=jpg
     *
     * Returns the uploadUrl (used by the client for the PUT request) and the objectKey
     * (which the client must send back to the backend to persist in the database).
     */
    @GetMapping("/generate-upload-url")
    public ResponseEntity<ApiResponse<PresignedUploadResponse>> generateUploadUrl(
            @RequestParam String folder,
            @RequestParam String extension) {
        log.info("GET /api/v1/images/generate-upload-url - folder: {}, extension: {}", folder, extension);

        S3Service.PresignedUpload result = s3Service.generateUploadUrl(extension, folder);
        PresignedUploadResponse response = PresignedUploadResponse.builder()
                .uploadUrl(result.uploadUrl())
                .objectKey(result.objectKey())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Pre-signed upload URL generated", response));
    }

    /**
     * Generates a pre-signed GET URL for a private S3 object, valid for 60 minutes.
     *
     * GET /api/v1/images/generate-download-url?key=transformers/base/abc123.jpg
     */
    @GetMapping("/generate-download-url")
    public ResponseEntity<ApiResponse<String>> generateDownloadUrl(
            @RequestParam String key) {
        log.info("GET /api/v1/images/generate-download-url - key: {}", key);

        String downloadUrl = s3Service.generateDownloadUrl(key);
        return ResponseEntity.ok(ApiResponse.success("Pre-signed download URL generated", downloadUrl));
    }
}
