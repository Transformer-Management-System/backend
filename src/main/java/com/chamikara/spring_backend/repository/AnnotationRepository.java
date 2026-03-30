package com.chamikara.spring_backend.repository;

import com.chamikara.spring_backend.entity.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    
    List<Annotation> findByInspectionId(Long inspectionId);

    List<Annotation> findByInspectionIdOrderByCreatedAtAsc(Long inspectionId);

    Optional<Annotation> findByIdAndInspectionId(Long id, Long inspectionId);
    
    void deleteByInspectionId(Long inspectionId);
}
