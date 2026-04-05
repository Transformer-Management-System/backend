package com.chamikara.spring_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionResponse {
    
    private Long id;
    private Long transformerId;
    private String transformerNumber;
    private String date;
    private String inspectedDate;
    private String inspector;
    private String notes;
    private String status;
    private String maintenanceImage;
    private String annotatedImage;
    private String imageLevelLabel;
    private Integer anomalyCount;
}
