package com.agrosphere.repository;
import com.agrosphere.entity.Order;
import com.agrosphere.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByConsumerIdOrderByCreatedAtDesc(Long consumerId);
    List<Order> findByFarmerTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<Order> findByFarmerTenantIdAndStatus(Long tenantId, OrderStatus status);
    @Query("SELECT COUNT(o) FROM Order o") long countAllOrders();
    @Query("SELECT COALESCE(SUM(o.totalAmount),0) FROM Order o WHERE o.status='DELIVERED'") BigDecimal sumPlatformRevenue();
    @Query("SELECT COALESCE(SUM(o.totalAmount),0) FROM Order o WHERE o.farmerTenant.id=:tid AND o.status='DELIVERED'") BigDecimal sumRevenueByTenant(@Param("tid") Long tid);
    @Query("SELECT COUNT(o) FROM Order o WHERE o.farmerTenant.id=:tid AND o.status='PENDING'") long countPendingByTenant(@Param("tid") Long tid);
}
