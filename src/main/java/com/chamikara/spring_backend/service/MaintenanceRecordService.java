package com.chamikara.spring_backend.service;

import com.chamikara.spring_backend.dto.request.MaintenanceRecordRequest;
import com.chamikara.spring_backend.dto.response.MaintenanceRecordResponse;
import com.chamikara.spring_backend.entity.Inspection;
import com.chamikara.spring_backend.entity.MaintenanceRecord;
import com.chamikara.spring_backend.entity.Transformer;
import com.chamikara.spring_backend.exception.ResourceNotFoundException;
import com.chamikara.spring_backend.repository.MaintenanceRecordRepository;
import com.chamikara.spring_backend.repository.InspectionRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("legacy")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MaintenanceRecordService {
    
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final InspectionRepository inspectionRepository;
    private final TransformerService transformerService;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public List<MaintenanceRecordResponse> getAllRecords() {
        log.debug("Fetching all maintenance records");
        return maintenanceRecordRepository.findAllWithTransformer().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public MaintenanceRecordResponse getRecordById(Long id) {
        log.debug("Fetching maintenance record with id: {}", id);
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", "id", id));
        return mapToResponse(record);
    }
    
    public List<MaintenanceRecordResponse> getRecordsByTransformerId(Long transformerId) {
        log.debug("Fetching maintenance records for transformer: {}", transformerId);
        return maintenanceRecordRepository.findByTransformerIdOrderByTimestampDesc(transformerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public MaintenanceRecordResponse createRecord(MaintenanceRecordRequest request) {
        log.debug("Creating new maintenance record for transformer: {}", request.getTransformerId());
        
        Transformer transformer = transformerService.getTransformerEntity(request.getTransformerId());
        String now = LocalDateTime.now().format(FORMATTER);
        
        Inspection inspection = null;
        if (request.getInspectionId() != null) {
            inspection = inspectionRepository.findById(request.getInspectionId()).orElse(null);
        }
        
        MaintenanceRecord record = MaintenanceRecord.builder()
                .transformer(transformer)
                .inspection(inspection)
                .recordTimestamp(now)
                .engineerName(request.getEngineerName())
                .status(request.getStatus())
                .readings(request.getReadings())
                .recommendedAction(request.getRecommendedAction())
                .notes(request.getNotes())
                .annotatedImage(request.getAnnotatedImage())
                .anomalies(request.getAnomalies())
                .location(request.getLocation() != null ? request.getLocation() : transformer.getLocation())
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        log.info("Created maintenance record with id: {}", saved.getId());
        return mapToResponse(saved);
    }
    
    public MaintenanceRecordResponse updateRecord(Long id, MaintenanceRecordRequest request) {
        log.debug("Updating maintenance record with id: {}", id);
        
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", "id", id));
        
        String now = LocalDateTime.now().format(FORMATTER);
        
        if (request.getEngineerName() != null) record.setEngineerName(request.getEngineerName());
        if (request.getStatus() != null) record.setStatus(request.getStatus());
        if (request.getReadings() != null) record.setReadings(request.getReadings());
        if (request.getRecommendedAction() != null) record.setRecommendedAction(request.getRecommendedAction());
        if (request.getNotes() != null) record.setNotes(request.getNotes());
        if (request.getAnnotatedImage() != null) record.setAnnotatedImage(request.getAnnotatedImage());
        if (request.getAnomalies() != null) record.setAnomalies(request.getAnomalies());
        if (request.getLocation() != null) record.setLocation(request.getLocation());
        record.setUpdatedAt(now);
        
        MaintenanceRecord updated = maintenanceRecordRepository.save(record);
        log.info("Updated maintenance record with id: {}", id);
        return mapToResponse(updated);
    }
    
    public void deleteRecord(Long id) {
        log.debug("Deleting maintenance record with id: {}", id);
        
        if (!maintenanceRecordRepository.existsById(id)) {
            throw new ResourceNotFoundException("MaintenanceRecord", "id", id);
        }
        
        maintenanceRecordRepository.deleteById(id);
        log.info("Deleted maintenance record with id: {}", id);
    }
    
    public byte[] exportToPdf(Long id) {
        log.debug("Exporting maintenance record to PDF: {}", id);
        
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", "id", id));
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Title
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(0, 51, 102));
            Paragraph title = new Paragraph("Maintenance Record Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Record Info Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10);
            infoTable.setSpacingAfter(20);
            
            addTableRow(infoTable, "Record ID", String.valueOf(record.getId()));
            addTableRow(infoTable, "Transformer Number", record.getTransformer().getNumber());
            addTableRow(infoTable, "Location", record.getLocation() != null ? record.getLocation() : "N/A");
            addTableRow(infoTable, "Engineer Name", record.getEngineerName() != null ? record.getEngineerName() : "N/A");
            addTableRow(infoTable, "Status", record.getStatus() != null ? record.getStatus() : "N/A");
            addTableRow(infoTable, "Record Timestamp", record.getRecordTimestamp());
            
            document.add(infoTable);
            
            // Readings Section
            if (record.getReadings() != null && !record.getReadings().isEmpty()) {
                Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0, 51, 102));
                Paragraph readingsTitle = new Paragraph("Electrical Readings", sectionFont);
                readingsTitle.setSpacingAfter(10);
                document.add(readingsTitle);
                
                Paragraph readings = new Paragraph(record.getReadings());
                readings.setSpacingAfter(15);
                document.add(readings);
            }
            
            // Recommended Action
            if (record.getRecommendedAction() != null && !record.getRecommendedAction().isEmpty()) {
                Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0, 51, 102));
                Paragraph actionTitle = new Paragraph("Recommended Action", sectionFont);
                actionTitle.setSpacingAfter(10);
                document.add(actionTitle);
                
                Paragraph action = new Paragraph(record.getRecommendedAction());
                action.setSpacingAfter(15);
                document.add(action);
            }
            
            // Notes
            if (record.getNotes() != null && !record.getNotes().isEmpty()) {
                Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0, 51, 102));
                Paragraph notesTitle = new Paragraph("Notes", sectionFont);
                notesTitle.setSpacingAfter(10);
                document.add(notesTitle);
                
                Paragraph notes = new Paragraph(record.getNotes());
                notes.setSpacingAfter(15);
                document.add(notes);
            }
            
            // Footer
            Font footerFont = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);
            Paragraph footer = new Paragraph(
                    "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    footerFont
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);
            
            document.close();
            
            log.info("Generated PDF for maintenance record: {}", id);
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Failed to generate PDF for maintenance record: {}", id, e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
    
    private void addTableRow(PdfPTable table, String label, String value) {
        Font labelFont = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
        
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(new Color(240, 240, 240));
        labelCell.setPadding(8);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);
        table.addCell(valueCell);
    }
    
    private MaintenanceRecordResponse mapToResponse(MaintenanceRecord record) {
        return MaintenanceRecordResponse.builder()
                .id(record.getId())
                .transformerId(record.getTransformer().getId())
                .transformerNumber(record.getTransformer().getNumber())
                .inspectionId(record.getInspection() != null ? record.getInspection().getId() : null)
                .recordTimestamp(record.getRecordTimestamp())
                .engineerName(record.getEngineerName())
                .status(record.getStatus())
                .readings(record.getReadings())
                .recommendedAction(record.getRecommendedAction())
                .notes(record.getNotes())
                .annotatedImage(record.getAnnotatedImage())
                .anomalies(record.getAnomalies())
                .location(record.getLocation())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
