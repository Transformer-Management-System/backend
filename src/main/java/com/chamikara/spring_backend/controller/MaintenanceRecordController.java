package com.chamikara.spring_backend.controller;

import com.chamikara.spring_backend.dto.request.MaintenanceRecordRequest;
import com.chamikara.spring_backend.dto.response.ApiResponse;
import com.chamikara.spring_backend.dto.response.MaintenanceRecordResponse;
import com.chamikara.spring_backend.service.MaintenanceRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/records")
@Profile("legacy")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MaintenanceRecordController {
    
    private final MaintenanceRecordService maintenanceRecordService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<MaintenanceRecordResponse>>> getAllRecords(
            @RequestParam(required = false, name = "transformer_id") Long transformerId) {
        log.info("GET /records - Fetching records, transformerId: {}", transformerId);
        
        List<MaintenanceRecordResponse> records;
        if (transformerId != null) {
            records = maintenanceRecordService.getRecordsByTransformerId(transformerId);
        } else {
            records = maintenanceRecordService.getAllRecords();
        }
        
        return ResponseEntity.ok(ApiResponse.success("Records retrieved successfully", records));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> getRecordById(@PathVariable Long id) {
        log.info("GET /records/{} - Fetching record", id);
        MaintenanceRecordResponse record = maintenanceRecordService.getRecordById(id);
        return ResponseEntity.ok(ApiResponse.success("Record retrieved successfully", record));
    }
    
    @GetMapping("/transformer/{transformerId}")
    public ResponseEntity<ApiResponse<List<MaintenanceRecordResponse>>> getRecordsByTransformerId(
            @PathVariable Long transformerId) {
        log.info("GET /records/transformer/{} - Fetching records", transformerId);
        List<MaintenanceRecordResponse> records = maintenanceRecordService.getRecordsByTransformerId(transformerId);
        return ResponseEntity.ok(ApiResponse.success("Records retrieved successfully", records));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> createRecord(
            @Valid @RequestBody MaintenanceRecordRequest request) {
        log.info("POST /records - Creating new record for transformer: {}", request.getTransformerId());
        MaintenanceRecordResponse created = maintenanceRecordService.createRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Record created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> updateRecord(
            @PathVariable Long id,
            @RequestBody MaintenanceRecordRequest request) {
        log.info("PUT /records/{} - Updating record", id);
        MaintenanceRecordResponse updated = maintenanceRecordService.updateRecord(id, request);
        return ResponseEntity.ok(ApiResponse.success("Record updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        log.info("DELETE /records/{} - Deleting record", id);
        maintenanceRecordService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponse.success("Record deleted successfully", null));
    }
    
    @GetMapping("/export/pdf/{id}")
    public ResponseEntity<byte[]> exportToPdf(@PathVariable Long id) {
        log.info("GET /records/export/pdf/{} - Exporting record to PDF", id);
        byte[] pdfBytes = maintenanceRecordService.exportToPdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "maintenance_record_" + id + ".pdf");
        headers.setContentLength(pdfBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
