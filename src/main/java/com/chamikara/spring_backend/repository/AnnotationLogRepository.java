package com.chamikara.spring_backend.repository;

import com.chamikara.spring_backend.entity.AnnotationLog;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("legacy")
@NoRepositoryBean
public interface AnnotationLogRepository extends JpaRepository<AnnotationLog, Long> {
    
    List<AnnotationLog> findByInspectionId(Long inspectionId);
    
    List<AnnotationLog> findByTransformerId(Long transformerId);
    
    List<AnnotationLog> findByActionType(String actionType);
    
    @Query("SELECT al FROM AnnotationLog al ORDER BY al.timestamp DESC")
    List<AnnotationLog> findAllOrderByTimestampDesc();
    
    @Query("SELECT al FROM AnnotationLog al JOIN FETCH al.inspection JOIN FETCH al.transformer")
    List<AnnotationLog> findAllWithRelations();
}
