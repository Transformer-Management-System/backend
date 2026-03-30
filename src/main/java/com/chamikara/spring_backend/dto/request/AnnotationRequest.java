package com.chamikara.spring_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnotationRequest {
    
    private String annotationId;
    
    @NotNull(message = "X coordinate is required")
    private Double x;
    
    @NotNull(message = "Y coordinate is required")
    private Double y;
    
    @NotNull(message = "Width is required")
    private Double w;
    
    @NotNull(message = "Height is required")
    private Double h;
    
    private Double confidence;
    
    private String severity;
    
    private String classification;
    
    private String comment;
    
    private String source;
    
    private Boolean deleted;
    
    private String userId;
}
