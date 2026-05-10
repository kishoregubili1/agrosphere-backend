package com.agrosphere.repository;
import com.agrosphere.entity.SoilTest;
import com.agrosphere.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SoilTestRepository extends JpaRepository<SoilTest, Long> {
    List<SoilTest> findByTenantOrderByTestDateDesc(Tenant tenant);
}
