package com.chamikara.spring_backend.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnotationLog {

    private Long id;

    private Inspection inspection;

    private Transformer transformer;

    private String imageId; // Reference to the annotated image

    private String actionType; // 'added', 'edited', 'deleted', 'ai_generated'

    private String annotationData; // JSON string of the annotation state

    private String aiPrediction; // Original AI prediction (JSON) if applicable

    private String userAnnotation; // Final user-modified annotation (JSON)

    @Builder.Default
    private String userId = "Admin";

    private String timestamp;

    private String notes;
}
