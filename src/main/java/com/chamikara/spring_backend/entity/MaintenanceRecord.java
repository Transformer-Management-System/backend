package com.chamikara.spring_backend.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecord {

    private Long id;

    private Transformer transformer;

    private Inspection inspection;

    private String recordTimestamp;

    private String engineerName;

    private String status; // OK / Needs Maintenance / Urgent Attention

    private String readings; // JSON string of key-value pairs

    private String recommendedAction;

    private String notes;

    private String annotatedImage; // snapshot of annotated image

    private String anomalies; // JSON array of anomaly objects

    private String location; // snapshot of transformer location

    private String createdAt;

    private String updatedAt;
}
