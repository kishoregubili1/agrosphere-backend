package com.agrosphere.repository;
import com.agrosphere.entity.Disease;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    List<Disease> findByIsActiveTrue();
}
