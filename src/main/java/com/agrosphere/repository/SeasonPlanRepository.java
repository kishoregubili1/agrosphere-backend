package com.agrosphere.repository;
import com.agrosphere.entity.SeasonPlan;
import com.agrosphere.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SeasonPlanRepository extends JpaRepository<SeasonPlan, Long> {
    List<SeasonPlan> findByTenantOrderByYearDescSeasonAsc(Tenant tenant);
}
