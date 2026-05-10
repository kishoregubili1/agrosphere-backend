package com.agrosphere.repository;
import com.agrosphere.entity.InventoryItem;
import com.agrosphere.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByTenantOrderByCreatedAtDesc(Tenant tenant);
}
