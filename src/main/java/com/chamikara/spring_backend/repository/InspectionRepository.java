package com.chamikara.spring_backend.repository;

import com.chamikara.spring_backend.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    
    List<Inspection> findByTransformerId(Long transformerId);
    
    List<Inspection> findByStatus(String status);
    
    @Query("SELECT i FROM Inspection i WHERE i.transformer.id = :transformerId ORDER BY i.inspectionDate DESC")
    List<Inspection> findByTransformerIdOrderByDateDesc(@Param("transformerId") Long transformerId);
    
    @Query("SELECT i FROM Inspection i JOIN FETCH i.transformer")
    List<Inspection> findAllWithTransformer();
}
