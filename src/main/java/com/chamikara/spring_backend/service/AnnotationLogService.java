package com.chamikara.spring_backend.service;

import com.chamikara.spring_backend.dto.response.AnnotationLogResponse;
import com.chamikara.spring_backend.entity.AnnotationLog;
import com.chamikara.spring_backend.repository.AnnotationLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("legacy")
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnnotationLogService {
    
    private final AnnotationLogRepository annotationLogRepository;
    private final ObjectMapper objectMapper;
    
    public List<AnnotationLogResponse> getAllAnnotationLogs() {
        log.debug("Fetching all annotation logs");
        return annotationLogRepository.findAllWithRelations().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<AnnotationLogResponse> getAnnotationLogsByInspectionId(Long inspectionId) {
        log.debug("Fetching annotation logs for inspection: {}", inspectionId);
        return annotationLogRepository.findByInspectionId(inspectionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public String exportToJson() {
        log.debug("Exporting annotation logs to JSON");
        try {
            List<AnnotationLogResponse> logs = getAllAnnotationLogs();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logs);
        } catch (Exception e) {
            log.error("Failed to export annotation logs to JSON", e);
            throw new RuntimeException("Failed to export annotation logs to JSON", e);
        }
    }
    
    public String exportToCsv() {
        log.debug("Exporting annotation logs to CSV");
        try {
            List<AnnotationLogResponse> logs = getAllAnnotationLogs();
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer);
            
            // Write header
            String[] header = {
                "ID", "Inspection ID", "Transformer ID", "Transformer Number",
                "Image ID", "Action Type", "Annotation Data", "AI Prediction",
                "User Annotation", "User ID", "Timestamp", "Notes"
            };
            csvWriter.writeNext(header);
            
            // Write data
            for (AnnotationLogResponse log : logs) {
                String[] row = {
                    String.valueOf(log.getId()),
                    String.valueOf(log.getInspectionId()),
                    String.valueOf(log.getTransformerId()),
                    log.getTransformerNumber(),
                    log.getImageId(),
                    log.getActionType(),
                    log.getAnnotationData(),
                    log.getAiPrediction(),
                    log.getUserAnnotation(),
                    log.getUserId(),
                    log.getTimestamp(),
                    log.getNotes()
                };
                csvWriter.writeNext(row);
            }
            
            csvWriter.close();
            return writer.toString();
        } catch (Exception e) {
            log.error("Failed to export annotation logs to CSV", e);
            throw new RuntimeException("Failed to export annotation logs to CSV", e);
        }
    }
    
    private AnnotationLogResponse mapToResponse(AnnotationLog log) {
        return AnnotationLogResponse.builder()
                .id(log.getId())
                .inspectionId(log.getInspection().getId())
                .transformerId(log.getTransformer().getId())
                .transformerNumber(log.getTransformer().getNumber())
                .imageId(log.getImageId())
                .actionType(log.getActionType())
                .annotationData(log.getAnnotationData())
                .aiPrediction(log.getAiPrediction())
                .userAnnotation(log.getUserAnnotation())
                .userId(log.getUserId())
                .timestamp(log.getTimestamp())
                .notes(log.getNotes())
                .build();
    }
}
