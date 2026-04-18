package com.chamikara.spring_backend.service;

import com.chamikara.spring_backend.dto.request.InspectionRequest;
import com.chamikara.spring_backend.dto.response.InspectionResponse;
import com.chamikara.spring_backend.entity.Inspection;
import com.chamikara.spring_backend.entity.Transformer;
import com.chamikara.spring_backend.exception.ResourceNotFoundException;
import com.chamikara.spring_backend.repository.InspectionRepository;
import com.chamikara.spring_backend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InspectionService {
    
    private final InspectionRepository inspectionRepository;
    private final TransformerService transformerService;
    private final CurrentUserService currentUserService;
    
    public List<InspectionResponse> getAllInspections() {
        log.debug("Fetching all inspections");
        return inspectionRepository.findAllWithTransformer().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public InspectionResponse getInspectionById(Long id) {
        log.debug("Fetching inspection with id: {}", id);
        Inspection inspection = inspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", "id", id));
        return mapToResponse(inspection);
    }
    
    public List<InspectionResponse> getInspectionsByTransformerId(Long transformerId) {
        log.debug("Fetching inspections for transformer: {}", transformerId);
        return inspectionRepository.findByTransformerIdOrderByDateDesc(transformerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public InspectionResponse createInspection(Long transformerId, InspectionRequest request) {
        log.debug("Creating new inspection for transformer: {}", transformerId);
        Long currentUserId = currentUserService.getCurrentUserIdOrNull();
        
        Transformer transformer = transformerService.getTransformerEntity(transformerId);
        LocalDate inspectionDate = resolveInspectionDate(request);

        if (request.getInspector() == null || request.getInspector().isBlank()) {
            throw new IllegalArgumentException("Inspector name is required");
        }
        
        Inspection inspection = Inspection.builder()
                .transformer(transformer)
                .inspector(request.getInspector())
                .inspectionDate(inspectionDate)
                .notes(request.getNotes())
                .status(request.getStatus() != null ? request.getStatus() : "SCHEDULED")
                .inspectionImageKey(resolveInspectionImageKey(request))
                .build();
        
        Inspection saved = inspectionRepository.save(inspection);
        log.info("Created inspection with id: {} by local user: {}", saved.getId(), currentUserId);
        return mapToResponse(saved);
    }
    
    public InspectionResponse updateInspection(Long id, InspectionRequest request) {
        log.debug("Updating inspection with id: {}", id);
        
        Inspection inspection = inspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", "id", id));
        
        if (request.getTransformerId() != null && 
                !request.getTransformerId().equals(inspection.getTransformer().getId())) {
            Transformer transformer = transformerService.getTransformerEntity(request.getTransformerId());
            inspection.setTransformer(transformer);
        }
        
        if (request.getDate() != null || request.getInspectedDate() != null) {
            inspection.setInspectionDate(resolveInspectionDate(request));
        }
        if (request.getInspector() != null) inspection.setInspector(request.getInspector());
        if (request.getNotes() != null) inspection.setNotes(request.getNotes());
        if (request.getStatus() != null) inspection.setStatus(request.getStatus());
        if (request.getMaintenanceImage() != null || request.getAnnotatedImage() != null) {
            inspection.setInspectionImageKey(resolveInspectionImageKey(request));
        }
        
        Inspection updated = inspectionRepository.save(inspection);
        log.info("Updated inspection with id: {}", id);
        return mapToResponse(updated);
    }
    
    public void deleteInspection(Long id) {
        log.debug("Deleting inspection with id: {}", id);
        
        if (!inspectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inspection", "id", id);
        }
        
        inspectionRepository.deleteById(id);
        log.info("Deleted inspection with id: {}", id);
    }
    
    public Inspection getInspectionEntity(Long id) {
        return inspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", "id", id));
    }
    
    private InspectionResponse mapToResponse(Inspection inspection) {
        String dateValue = inspection.getInspectionDate() != null ? inspection.getInspectionDate().toString() : null;
        return InspectionResponse.builder()
                .id(inspection.getId())
                .transformerId(inspection.getTransformer().getId())
                .transformerNumber(inspection.getTransformer().getNumber())
                .date(dateValue)
                .inspectedDate(dateValue)
                .inspector(inspection.getInspector())
                .notes(inspection.getNotes())
                .status(inspection.getStatus())
                .maintenanceImage(inspection.getInspectionImageKey())
                .annotatedImage(inspection.getInspectionImageKey())
                .annotatedImageKey(inspection.getAnnotatedImageKey())
                .imageLevelLabel(inspection.getImageLevelLabel())
                .anomalyCount(inspection.getAnomalyCount())
                .build();
    }

    private LocalDate resolveInspectionDate(InspectionRequest request) {
        String rawDate = request.getInspectedDate() != null && !request.getInspectedDate().isBlank()
                ? request.getInspectedDate()
                : request.getDate();

        if (rawDate == null || rawDate.isBlank()) {
            throw new IllegalArgumentException("Inspection date is required (use date or inspectedDate)");
        }

        try {
            return LocalDate.parse(rawDate);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Inspection date must be in yyyy-MM-dd format");
        }
    }

    private String resolveInspectionImageKey(InspectionRequest request) {
        if (request.getMaintenanceImage() != null && !request.getMaintenanceImage().isBlank()) {
            return request.getMaintenanceImage();
        }
        return request.getAnnotatedImage();
    }
}
