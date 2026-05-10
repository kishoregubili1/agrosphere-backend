package com.agrosphere.repository;
import com.agrosphere.entity.ActivityTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityTemplateRepository extends JpaRepository<ActivityTemplate, Long> {
    List<ActivityTemplate> findByCropTypeId(Long cropTypeId);
    List<ActivityTemplate> findByCropTypeIdAndIsActiveTrue(Long cropTypeId);
}
