package com.chamikara.spring_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionRequest {
    
    private Long transformerId;
    
    private String date;
    
    private String inspectedDate;
    
    @NotBlank(message = "Inspector name is required")
    private String inspector;
    
    private String notes;
    
    private String status;
    
    private String maintenanceImage;
    
    private String maintenanceUploadDate;
    
    private String maintenanceWeather;
    
    private String annotatedImage;
    
    private String anomalies;
    
    private String progressStatus;
}
