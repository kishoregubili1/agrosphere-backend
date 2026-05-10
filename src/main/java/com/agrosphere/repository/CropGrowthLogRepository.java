package com.agrosphere.repository;
import com.agrosphere.entity.Crop;
import com.agrosphere.entity.CropGrowthLog;
import com.agrosphere.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CropGrowthLogRepository extends JpaRepository<CropGrowthLog, Long> {
    List<CropGrowthLog> findByCropOrderByLoggedAtDesc(Crop crop);
    List<CropGrowthLog> findByTenantOrderByLoggedAtDesc(Tenant tenant);
}
