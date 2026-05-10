package com.agrosphere.repository;
import com.agrosphere.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TenantRepository extends JpaRepository<Tenant, Long> {}
