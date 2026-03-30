package com.chamikara.spring_backend.repository;

import com.chamikara.spring_backend.entity.MaintenanceRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("legacy")
@NoRepositoryBean
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    
    List<MaintenanceRecord> findByTransformerId(Long transformerId);
    
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.transformer.id = :transformerId ORDER BY mr.recordTimestamp DESC")
    List<MaintenanceRecord> findByTransformerIdOrderByTimestampDesc(@Param("transformerId") Long transformerId);
    
    List<MaintenanceRecord> findByStatus(String status);
    
    @Query("SELECT mr FROM MaintenanceRecord mr JOIN FETCH mr.transformer")
    List<MaintenanceRecord> findAllWithTransformer();
}
