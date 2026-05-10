package com.agrosphere.repository;
import com.agrosphere.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FieldRepository extends JpaRepository<Field, Long> {
    List<Field> findByFarmId(Long farmId);
    List<Field> findByFarm_TenantId(Long tenantId);
    List<Field> findByTenant_Id(Long tenantId);

    // Alias so services using findByTenantId still work
    default List<Field> findByTenantId(Long tenantId) {
        return findByTenant_Id(tenantId);
    }

    @Query("SELECT f FROM Field f WHERE f.farm.id = :farmId AND f.tenant.id = :tid")
    List<Field> findByFarmIdAndTenantId(@Param("farmId") Long farmId, @Param("tid") Long tid);

    @Query("SELECT f FROM Field f WHERE f.id = :id AND f.tenant.id = :tid")
    Optional<Field> findByIdAndTenantId(@Param("id") Long id, @Param("tid") Long tid);
}
