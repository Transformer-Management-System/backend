package com.chamikara.spring_backend.controller;

import com.chamikara.spring_backend.dto.request.AnnotationRequest;
import com.chamikara.spring_backend.dto.response.ApiResponse;
import com.chamikara.spring_backend.dto.response.AnnotationResponse;
import com.chamikara.spring_backend.service.AnnotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnnotationController {
    
    private final AnnotationService annotationService;
    
    @GetMapping("/inspections/{inspectionId}/anomalies")
    public ResponseEntity<ApiResponse<List<AnnotationResponse>>> getAnnotationsByInspectionId(
            @PathVariable Long inspectionId) {
        log.info("GET /api/v1/inspections/{}/anomalies - Fetching anomalies", inspectionId);
        List<AnnotationResponse> annotations = annotationService.getAnnotationsByInspectionId(inspectionId);
        return ResponseEntity.ok(ApiResponse.success("Annotations retrieved successfully", annotations));
    }
    
    @PostMapping("/inspections/{inspectionId}/anomalies")
    public ResponseEntity<ApiResponse<AnnotationResponse>> createAnnotation(
            @PathVariable Long inspectionId,
            @Valid @RequestBody AnnotationRequest request) {
        log.info("POST /api/v1/inspections/{}/anomalies - Creating anomaly", inspectionId);
        AnnotationResponse created = annotationService.createAnnotation(inspectionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Anomaly created successfully", created));
    }

    @PutMapping("/anomalies/{id}")
    public ResponseEntity<ApiResponse<AnnotationResponse>> updateAnnotation(
            @PathVariable Long id,
            @Valid @RequestBody AnnotationRequest request) {
        log.info("PUT /api/v1/anomalies/{} - Updating anomaly", id);
        AnnotationResponse updated = annotationService.updateAnnotation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Anomaly updated successfully", updated));
    }

    @DeleteMapping("/anomalies/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAnnotation(@PathVariable Long id) {
        log.info("DELETE /api/v1/anomalies/{} - Deleting anomaly", id);
        annotationService.deleteAnnotation(id);
        return ResponseEntity.ok(ApiResponse.success("Anomaly deleted successfully", null));
    }
}
