package com.agrosphere.repository;
import com.agrosphere.entity.CropActivity;
import com.agrosphere.enums.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface CropActivityRepository extends JpaRepository<CropActivity, Long> {
    List<CropActivity> findByCrop_TenantId(Long tenantId);
    void deleteByCropId(Long cropId);
    List<CropActivity> findByCrop_TenantIdAndStatus(Long tenantId, ActivityStatus status);

    @Query("SELECT a FROM CropActivity a WHERE a.crop.tenant.id = :tid ORDER BY a.scheduledDate ASC")
    List<CropActivity> findByTenantIdOrderByScheduledDateAsc(@Param("tid") Long tid);

    @Query("SELECT a FROM CropActivity a WHERE a.crop.tenant.id = :tid AND a.scheduledDate BETWEEN :from AND :to")
    List<CropActivity> findByTenantIdAndScheduledDateBetween(@Param("tid") Long tid, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT a FROM CropActivity a WHERE a.crop.tenant.id = :tid AND a.status = :status")
    List<CropActivity> findByTenantIdAndStatus(@Param("tid") Long tid, @Param("status") ActivityStatus status);
}