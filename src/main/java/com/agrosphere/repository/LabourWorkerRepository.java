package com.agrosphere.repository;
import com.agrosphere.entity.LabourWorker;
import com.agrosphere.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface LabourWorkerRepository extends JpaRepository<LabourWorker, Long> {
    List<LabourWorker> findByTenantOrderByCreatedAtDesc(Tenant tenant);
}
