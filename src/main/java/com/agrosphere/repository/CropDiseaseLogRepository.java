package com.agrosphere.repository;
import com.agrosphere.entity.CropDiseaseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CropDiseaseLogRepository extends JpaRepository<CropDiseaseLog, Long> {
    List<CropDiseaseLog> findByCrop_TenantId(Long tenantId);
    void deleteByCropId(Long cropId);
    long countByCrop_TenantIdAndIsResolvedFalse(Long tenantId);

    // Used by DiseaseService
    List<CropDiseaseLog> findByTenant_IdOrderByCreatedAtDesc(Long tenantId);
    List<CropDiseaseLog> findByTenant_IdAndIsResolvedFalse(Long tenantId);
}