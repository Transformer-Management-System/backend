package com.chamikara.spring_backend.controller;

import com.chamikara.spring_backend.dto.request.InspectionAnalysisRequest;
import com.chamikara.spring_backend.dto.response.AnomalyDetectionResponse;
import com.chamikara.spring_backend.dto.response.ApiResponse;
import com.chamikara.spring_backend.service.AnomalyDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionController {

    private final AnomalyDetectionService anomalyDetectionService;

    @PostMapping("/{id}/analyze")
    public ResponseEntity<ApiResponse<AnomalyDetectionResponse>> analyzeInspection(
            @PathVariable Long id,
            @RequestBody(required = false) InspectionAnalysisRequest request) {

        Double sliderPercent = request != null ? request.getSliderPercent() : null;
        log.info("POST /api/v1/inspections/{}/analyze - sliderPercent: {}", id, sliderPercent);

        AnomalyDetectionResponse response = anomalyDetectionService.analyzeInspection(id, sliderPercent);

        return ResponseEntity.ok(ApiResponse.success("Inspection analysis completed", response));
    }
}
