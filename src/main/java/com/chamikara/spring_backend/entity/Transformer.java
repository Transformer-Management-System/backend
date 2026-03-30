package com.chamikara.spring_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transformers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transformer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "transformer_number", unique = true, nullable = false)
    private String number;

    @Column(name = "pole_number")
    private String pole;

    private String region;

    private String type;

    @Column(name = "baseline_image_key")
    private String baselineImage;

    private String weather;

    private String location;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "transformer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Inspection> inspections = new ArrayList<>();
}
