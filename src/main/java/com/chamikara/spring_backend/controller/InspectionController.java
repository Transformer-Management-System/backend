package com.chamikara.spring_backend.controller;

import com.chamikara.spring_backend.dto.request.InspectionRequest;
import com.chamikara.spring_backend.dto.response.ApiResponse;
import com.chamikara.spring_backend.dto.response.InspectionResponse;
import com.chamikara.spring_backend.security.AuthenticatedUser;
import com.chamikara.spring_backend.service.InspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class InspectionController {
    
    private final InspectionService inspectionService;
    
    @GetMapping("/inspections/{id}")
    public ResponseEntity<ApiResponse<InspectionResponse>> getInspectionById(@PathVariable Long id) {
        log.info("GET /api/v1/inspections/{} - Fetching inspection", id);
        InspectionResponse inspection = inspectionService.getInspectionById(id);
        return ResponseEntity.ok(ApiResponse.success("Inspection retrieved successfully", inspection));
    }
    
    @GetMapping("/transformers/{transformerId}/inspections")
    public ResponseEntity<ApiResponse<List<InspectionResponse>>> getInspectionsByTransformerId(
            @PathVariable Long transformerId) {
        log.info("GET /api/v1/transformers/{}/inspections - Fetching inspections", transformerId);
        List<InspectionResponse> inspections = inspectionService.getInspectionsByTransformerId(transformerId);
        return ResponseEntity.ok(ApiResponse.success("Inspections retrieved successfully", inspections));
    }
    
    @PostMapping("/transformers/{transformerId}/inspections")
    public ResponseEntity<ApiResponse<InspectionResponse>> createInspection(
            @PathVariable Long transformerId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody InspectionRequest request) {
        log.info("POST /api/v1/transformers/{}/inspections - Creating inspection by local user: {}",
            transformerId,
            currentUser != null ? currentUser.localUserId() : null);
        InspectionResponse created = inspectionService.createInspection(transformerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inspection created successfully", created));
    }
    
    @PutMapping("/inspections/{id}")
    public ResponseEntity<ApiResponse<InspectionResponse>> updateInspection(
            @PathVariable Long id,
            @RequestBody InspectionRequest request) {
        log.info("PUT /api/v1/inspections/{} - Updating inspection", id);
        InspectionResponse updated = inspectionService.updateInspection(id, request);
        return ResponseEntity.ok(ApiResponse.success("Inspection updated successfully", updated));
    }
    
    @DeleteMapping("/inspections/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInspection(@PathVariable Long id) {
        log.info("DELETE /api/v1/inspections/{} - Deleting inspection", id);
        inspectionService.deleteInspection(id);
        return ResponseEntity.ok(ApiResponse.success("Inspection deleted successfully", null));
    }
}
