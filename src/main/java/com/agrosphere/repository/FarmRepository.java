package com.agrosphere.repository;
import com.agrosphere.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByTenantId(Long tenantId);
    List<Farm> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<Farm> findByIdAndTenantId(Long id, Long tenantId);
}
