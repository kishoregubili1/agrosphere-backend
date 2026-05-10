package com.agrosphere.repository;
import com.agrosphere.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.tenant WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findByIsActiveTrue();
    List<Product> findByTenantId(Long tenantId);
    List<Product> findByTenantIdAndIsActiveTrue(Long tenantId);
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.tenant WHERE p.category = :cat AND p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findByCategoryAndIsActiveTrue(@Param("cat") String category);
    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);
    void deleteByCropId(Long cropId);
}