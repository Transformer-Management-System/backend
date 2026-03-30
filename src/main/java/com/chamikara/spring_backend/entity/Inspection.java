package com.chamikara.spring_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inspections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transformer_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Transformer transformer;

    @Column(name = "inspector_name", nullable = false)
    private String inspector;

    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "status")
    @Builder.Default
    private String status = "SCHEDULED";

    @Column(name = "inspection_image_key")
    private String inspectionImageKey;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Annotation> annotations = new ArrayList<>();
}
