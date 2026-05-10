package com.agrosphere.repository;
import com.agrosphere.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findByTenantId(Long tenantId);
    default List<Crop> findByTenantIdAndIsActiveTrue(Long tenantId) { return findByTenantId(tenantId); }
    Optional<Crop> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT c FROM Crop c WHERE c.field.id = :fid AND c.tenant.id = :tid")
    List<Crop> findByFieldIdAndTenantId(@Param("fid") Long fieldId, @Param("tid") Long tenantId);
}
