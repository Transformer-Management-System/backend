package com.chamikara.spring_backend.service;

import com.chamikara.spring_backend.dto.response.AnomalyDetectionResponse;
import com.chamikara.spring_backend.entity.Annotation;
import com.chamikara.spring_backend.entity.Inspection;
import com.chamikara.spring_backend.exception.ResourceNotFoundException;
import com.chamikara.spring_backend.exception.ServiceException;
import com.chamikara.spring_backend.repository.AnnotationRepository;
import com.chamikara.spring_backend.repository.InspectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AnomalyDetectionService {

    private final WebClient webClient;
    private final S3Service s3Service;
    private final InspectionRepository inspectionRepository;
    private final AnnotationRepository annotationRepository;
    private final long timeout;

    public AnomalyDetectionService(
            WebClient webClient,
            S3Service s3Service,
            InspectionRepository inspectionRepository,
            AnnotationRepository annotationRepository,
            @Value("${fastapi.service.timeout:60000}") long timeout) {
        this.webClient = webClient;
        this.s3Service = s3Service;
        this.inspectionRepository = inspectionRepository;
        this.annotationRepository = annotationRepository;
        this.timeout = timeout;
    }

    @Transactional
    public AnomalyDetectionResponse analyzeInspection(Long inspectionId, Double sliderPercent) {
        log.info("Starting analysis for inspection: {}", inspectionId);

        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", "id", inspectionId));

        String maintenanceKey = inspection.getInspectionImageKey();
        String baselineKey = inspection.getTransformer().getBaselineImage();

        if (baselineKey == null || baselineKey.isBlank()) {
            throw new IllegalArgumentException("Baseline image is missing for the inspection's transformer");
        }
        if (maintenanceKey == null || maintenanceKey.isBlank()) {
            throw new IllegalArgumentException("Inspection image is missing for analysis");
        }

        String baselineUrl = s3Service.generateDownloadUrl(baselineKey);
        String maintenanceUrl = s3Service.generateDownloadUrl(maintenanceKey);

        AnomalyDetectionResponse response = callDetectionService(baselineUrl, maintenanceUrl, sliderPercent);

        saveDetectionResults(inspection, response);

        return response;
    }

    private AnomalyDetectionResponse callDetectionService(
            String baselineUrl, String maintenanceUrl, Double sliderPercent) {
        log.info("Calling anomaly detection microservice");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("baseline_url", baselineUrl);
        requestBody.put("maintenance_url", maintenanceUrl);
        if (sliderPercent != null) {
            requestBody.put("slider_percent", sliderPercent);
        }

        try {
            AnomalyDetectionResponse response = webClient.post()
                    .uri("/api/v1/detect")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(AnomalyDetectionResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response == null) {
                throw new ServiceException("Detection service returned an empty response");
            }

            log.info("Detection completed — imageLevelLabel: {}, anomalyCount: {}",
                    response.getImageLevelLabel(), response.getAnomalyCount());
            return response;

        } catch (WebClientResponseException ex) {
            log.error("Detection service returned HTTP {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ServiceException("Detection service error: " + ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to call detection service", ex);
            throw new ServiceException("Detection service call failed: " + ex.getMessage(), ex);
        }
    }

    private void saveDetectionResults(Inspection inspection, AnomalyDetectionResponse response) {
        // Update inspection-level metadata
        inspection.setImageLevelLabel(response.getImageLevelLabel());
        inspection.setAnomalyCount(response.getAnomalyCount());
        inspection.setDetectionRequestId(response.getRequestId());
        inspection.setDetectionMetrics(mapMetrics(response.getMetrics()));
        inspectionRepository.save(inspection);

        // Remove previous AI-detected anomalies (keep manual ones)
        annotationRepository.deleteByInspectionIdAndManuallyVerified(inspection.getId(), false);

        // Persist new anomalies
        List<AnomalyDetectionResponse.DetectedAnomaly> detected = response.getAnomalies();
        if (detected == null || detected.isEmpty()) {
            return;
        }

        for (AnomalyDetectionResponse.DetectedAnomaly anomaly : detected) {
            Annotation entity = Annotation.builder()
                    .inspection(inspection)
                    .label(anomaly.getClassification())
                    .confidenceScore(toBigDecimal(anomaly.getConfidence()))
                    .boundingBox(mapBbox(anomaly.getBbox()))
                    .severity(anomaly.getSeverity())
                    .severityScore(toBigDecimal(anomaly.getSeverityScore()))
                    .classification(anomaly.getClassification())
                    .area(anomaly.getArea())
                    .centroid(mapCentroid(anomaly.getCentroid()))
                    .meanDeltaE(toBigDecimal(anomaly.getMeanDeltaE()))
                    .peakDeltaE(toBigDecimal(anomaly.getPeakDeltaE()))
                    .meanHsv(mapHsv(anomaly.getMeanHsv()))
                    .elongation(toBigDecimal(anomaly.getElongation()))
                    .manuallyVerified(false)
                    .build();
            annotationRepository.save(entity);
        }

        log.info("Saved {} anomalies for inspection {}", detected.size(), inspection.getId());
    }

    // ── mapping helpers ──

    private Map<String, Object> mapMetrics(AnomalyDetectionResponse.DetectionMetrics m) {
        if (m == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("meanSsim", m.getMeanSsim());
        map.put("warpModel", m.getWarpModel());
        map.put("warpSuccess", m.getWarpSuccess());
        map.put("warpScore", m.getWarpScore());
        map.put("thresholdPotential", m.getThresholdPotential());
        map.put("thresholdFault", m.getThresholdFault());
        map.put("basePotential", m.getBasePotential());
        map.put("baseFault", m.getBaseFault());
        map.put("sliderPercent", m.getSliderPercent());
        map.put("scaleApplied", m.getScaleApplied());
        map.put("thresholdSource", m.getThresholdSource());
        map.put("ratio", m.getRatio());
        return map;
    }

    private Map<String, Double> mapBbox(AnomalyDetectionResponse.BoundingBox b) {
        if (b == null) return null;
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("x", b.getX() != null ? b.getX().doubleValue() : null);
        map.put("y", b.getY() != null ? b.getY().doubleValue() : null);
        map.put("width", b.getWidth() != null ? b.getWidth().doubleValue() : null);
        map.put("height", b.getHeight() != null ? b.getHeight().doubleValue() : null);
        return map;
    }

    private Map<String, Double> mapCentroid(AnomalyDetectionResponse.Centroid c) {
        if (c == null) return null;
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("x", c.getX());
        map.put("y", c.getY());
        return map;
    }

    private Map<String, Double> mapHsv(AnomalyDetectionResponse.MeanHsv hsv) {
        if (hsv == null) return null;
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("h", hsv.getH());
        map.put("s", hsv.getS());
        map.put("v", hsv.getV());
        return map;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    public boolean isServiceHealthy() {
        try {
            String response = webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return response != null && response.contains("healthy");
        } catch (Exception e) {
            log.warn("Detection service health check failed", e);
            return false;
        }
    }
}
