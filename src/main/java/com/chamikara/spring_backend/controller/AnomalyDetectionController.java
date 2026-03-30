package com.chamikara.spring_backend.controller;

import com.chamikara.spring_backend.dto.request.InspectionAnalysisRequest;
import com.chamikara.spring_backend.dto.response.AnomalyDetectionResponse;
import com.chamikara.spring_backend.dto.response.ApiResponse;
import com.chamikara.spring_backend.service.AnomalyDetectionService;
import com.chamikara.spring_backend.service.InspectionService;
import com.chamikara.spring_backend.service.TransformerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnomalyDetectionController {
    
    private final AnomalyDetectionService anomalyDetectionService;
    private final InspectionService inspectionService;
    private final TransformerService transformerService;
    
    @PostMapping("/{id}/analyze")
    public ResponseEntity<ApiResponse<AnomalyDetectionResponse>> analyzeInspection(
            @PathVariable Long id,
            @RequestBody(required = false) InspectionAnalysisRequest request) {

        Double sliderPercent = request != null ? request.getSliderPercent() : null;

        log.info("POST /api/v1/inspections/{}/analyze - Triggering analysis", id);

        var inspection = inspectionService.getInspectionById(id);
        var transformer = transformerService.getTransformerById(inspection.getTransformerId());

        String transformerId = transformer.getNumber();
        String baselineImage = transformer.getBaselineImage();
        String maintenanceImage = inspection.getMaintenanceImage();

        if (baselineImage == null || baselineImage.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Baseline image is missing for the inspection's transformer"));
        }

        if (maintenanceImage == null || maintenanceImage.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Inspection image is missing for analysis"));
        }

        AnomalyDetectionResponse response = anomalyDetectionService.detectAnomalies(
                transformerId, baselineImage, maintenanceImage, sliderPercent);

        return ResponseEntity.ok(ApiResponse.success("Inspection analysis completed", response));
    }
}
