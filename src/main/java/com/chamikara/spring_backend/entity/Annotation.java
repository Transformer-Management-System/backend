package com.chamikara.spring_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "anomalies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Inspection inspection;

    @Column(name = "label")
    private String label;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bounding_box")
    private Map<String, Double> boundingBox;

    @Column(name = "is_manually_verified")
    @Builder.Default
    private Boolean manuallyVerified = false;

    @Column(name = "verified_by_user_id")
    private Long verifiedByUserId;

    @Column(name = "human_notes", columnDefinition = "TEXT")
    private String humanNotes;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
