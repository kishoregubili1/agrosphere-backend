package com.agrosphere.repository;
import com.agrosphere.entity.CropType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CropTypeRepository extends JpaRepository<CropType, Long> {
    List<CropType> findByIsActiveTrue();
}
