package com.agrosphere.repository;
import com.agrosphere.entity.LabourLog;
import com.agrosphere.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface LabourLogRepository extends JpaRepository<LabourLog, Long> {
    List<LabourLog> findByTenantOrderByWorkDateDescCreatedAtDesc(Tenant tenant);
}
