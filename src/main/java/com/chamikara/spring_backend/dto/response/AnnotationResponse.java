package com.chamikara.spring_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnotationResponse {
    
    private Long id;
    private Long inspectionId;
    private String annotationId;
    private Double x;
    private Double y;
    private Double w;
    private Double h;
    private Double confidence;
    private String severity;
    private Double severityScore;
    private String classification;
    private Integer area;
    private Map<String, Double> centroid;
    private Double meanDeltaE;
    private Double peakDeltaE;
    private Map<String, Double> meanHsv;
    private Double elongation;
    private String comment;
    private String source;
    private Boolean deleted;
    private String userId;
    private String createdAt;
    private String updatedAt;
}
