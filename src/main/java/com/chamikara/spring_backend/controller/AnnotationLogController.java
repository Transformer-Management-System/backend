package com.chamikara.spring_backend.controller;

import com.chamikara.spring_backend.dto.response.ApiResponse;
import com.chamikara.spring_backend.dto.response.AnnotationLogResponse;
import com.chamikara.spring_backend.service.AnnotationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/annotation-logs")
@Profile("legacy")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnnotationLogController {
    
    private final AnnotationLogService annotationLogService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<AnnotationLogResponse>>> getAllAnnotationLogs() {
        log.info("GET /annotation-logs - Fetching all annotation logs");
        List<AnnotationLogResponse> logs = annotationLogService.getAllAnnotationLogs();
        return ResponseEntity.ok(ApiResponse.success("Annotation logs retrieved successfully", logs));
    }
    
    @GetMapping("/inspection/{inspectionId}")
    public ResponseEntity<ApiResponse<List<AnnotationLogResponse>>> getAnnotationLogsByInspectionId(
            @PathVariable Long inspectionId) {
        log.info("GET /annotation-logs/inspection/{} - Fetching annotation logs", inspectionId);
        List<AnnotationLogResponse> logs = annotationLogService.getAnnotationLogsByInspectionId(inspectionId);
        return ResponseEntity.ok(ApiResponse.success("Annotation logs retrieved successfully", logs));
    }
    
    @GetMapping("/export/json")
    public ResponseEntity<String> exportToJson() {
        log.info("GET /annotation-logs/export/json - Exporting annotation logs to JSON");
        String json = annotationLogService.exportToJson();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "annotation_logs.json");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(json);
    }
    
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportToCsv() {
        log.info("GET /annotation-logs/export/csv - Exporting annotation logs to CSV");
        String csv = annotationLogService.exportToCsv();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "annotation_logs.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
}
