package com.chamikara.spring_backend.service;

import com.chamikara.spring_backend.dto.request.AnnotationRequest;
import com.chamikara.spring_backend.dto.response.AnnotationResponse;
import com.chamikara.spring_backend.entity.Annotation;
import com.chamikara.spring_backend.entity.Inspection;
import com.chamikara.spring_backend.exception.ResourceNotFoundException;
import com.chamikara.spring_backend.repository.AnnotationRepository;
import com.chamikara.spring_backend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnnotationService {
    
    private final AnnotationRepository annotationRepository;
    private final InspectionService inspectionService;
    private final CurrentUserService currentUserService;
    
    public List<AnnotationResponse> getAnnotationsByInspectionId(Long inspectionId) {
        log.debug("Fetching annotations for inspection: {}", inspectionId);
        return annotationRepository.findByInspectionIdOrderByCreatedAtAsc(inspectionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public AnnotationResponse createAnnotation(Long inspectionId, AnnotationRequest request) {
        log.debug("Creating anomaly for inspection: {}", inspectionId);

        Inspection inspection = inspectionService.getInspectionEntity(inspectionId);
        Long fallbackUserId = currentUserService.getCurrentUserIdOrNull();
        Annotation annotation = Annotation.builder()
                .inspection(inspection)
                .build();

        updateAnnotationFields(annotation, request, fallbackUserId);

        Annotation saved = annotationRepository.save(annotation);
        log.info("Created anomaly with id: {} for inspection: {}", saved.getId(), inspectionId);
        return mapToResponse(saved);
    }

    public AnnotationResponse updateAnnotation(Long anomalyId, AnnotationRequest request) {
        log.debug("Updating anomaly with id: {}", anomalyId);

        Annotation annotation = annotationRepository.findById(anomalyId)
                .orElseThrow(() -> new ResourceNotFoundException("Anomaly", "id", anomalyId));

        updateAnnotationFields(annotation, request, currentUserService.getCurrentUserIdOrNull());

        Annotation updated = annotationRepository.save(annotation);
        log.info("Updated anomaly with id: {}", anomalyId);
        return mapToResponse(updated);
    }

    public void deleteAnnotation(Long anomalyId) {
        log.debug("Deleting anomaly with id: {}", anomalyId);

        if (!annotationRepository.existsById(anomalyId)) {
            throw new ResourceNotFoundException("Anomaly", "id", anomalyId);
        }

        annotationRepository.deleteById(anomalyId);
        log.info("Deleted anomaly with id: {}", anomalyId);
    }

    private void updateAnnotationFields(Annotation annotation, AnnotationRequest request, Long fallbackUserId) {
        String source = request.getSource() != null && !request.getSource().isBlank()
                ? request.getSource()
                : "user";

        annotation.setLabel(resolveLabel(request));
        annotation.setConfidenceScore(request.getConfidence() != null
                ? BigDecimal.valueOf(request.getConfidence())
                : null);
        annotation.setBoundingBox(buildBoundingBox(request));

        Long verifiedUserId = parseUserId(request.getUserId());
        if (verifiedUserId == null) {
            verifiedUserId = fallbackUserId;
        }

        boolean manuallyVerified = "user".equalsIgnoreCase(source) || verifiedUserId != null;
        annotation.setManuallyVerified(manuallyVerified);
        annotation.setVerifiedByUserId(verifiedUserId);
        annotation.setHumanNotes(request.getComment());
    }

    private String resolveLabel(AnnotationRequest request) {
        if (request.getClassification() != null && !request.getClassification().isBlank()) {
            return request.getClassification();
        }
        if (request.getSeverity() != null && !request.getSeverity().isBlank()) {
            return request.getSeverity();
        }
        return "UNSPECIFIED";
    }

    private Map<String, Double> buildBoundingBox(AnnotationRequest request) {
        Map<String, Double> bbox = new LinkedHashMap<>();
        bbox.put("x", request.getX());
        bbox.put("y", request.getY());
        bbox.put("w", request.getW());
        bbox.put("h", request.getH());

        return bbox;
    }

    private Long parseUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BoundingBox parseBoundingBox(Map<String, Double> boundingBoxMap) {
        if (boundingBoxMap == null || boundingBoxMap.isEmpty()) {
            return new BoundingBox(null, null, null, null);
        }

        Double x = extractNumber(boundingBoxMap, "x");
        Double y = extractNumber(boundingBoxMap, "y");
        Double w = extractNumber(boundingBoxMap, "w", "width");
        Double h = extractNumber(boundingBoxMap, "h", "height");

        return new BoundingBox(x, y, w, h);
    }

    private Double extractNumber(Map<String, Double> values, String... keys) {
        for (String key : keys) {
            Double value = values.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private AnnotationResponse mapToResponse(Annotation annotation) {
        BoundingBox boundingBox = parseBoundingBox(annotation.getBoundingBox());
        String createdAt = annotation.getCreatedAt() != null ? annotation.getCreatedAt().toString() : null;

        return AnnotationResponse.builder()
                .id(annotation.getId())
                .inspectionId(annotation.getInspection() != null ? annotation.getInspection().getId() : null)
                .annotationId(annotation.getId() != null ? String.valueOf(annotation.getId()) : null)
                .x(boundingBox.x)
                .y(boundingBox.y)
                .w(boundingBox.w)
                .h(boundingBox.h)
                .confidence(annotation.getConfidenceScore() != null ? annotation.getConfidenceScore().doubleValue() : null)
                .severity(annotation.getSeverity())
                .severityScore(annotation.getSeverityScore() != null ? annotation.getSeverityScore().doubleValue() : null)
                .classification(annotation.getClassification())
                .area(annotation.getArea())
                .centroid(annotation.getCentroid())
                .meanDeltaE(annotation.getMeanDeltaE() != null ? annotation.getMeanDeltaE().doubleValue() : null)
                .peakDeltaE(annotation.getPeakDeltaE() != null ? annotation.getPeakDeltaE().doubleValue() : null)
                .meanHsv(annotation.getMeanHsv())
                .elongation(annotation.getElongation() != null ? annotation.getElongation().doubleValue() : null)
                .comment(annotation.getHumanNotes())
                .source(Boolean.TRUE.equals(annotation.getManuallyVerified()) ? "user" : "ai")
                .deleted(false)
                .userId(annotation.getVerifiedByUserId() != null ? String.valueOf(annotation.getVerifiedByUserId()) : null)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }

    private static class BoundingBox {
        private final Double x;
        private final Double y;
        private final Double w;
        private final Double h;

        private BoundingBox(Double x, Double y, Double w, Double h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}
