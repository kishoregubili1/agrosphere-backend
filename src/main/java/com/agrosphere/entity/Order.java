package com.agrosphere.entity;

import com.agrosphere.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name="orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="order_number",unique=true) private String orderNumber;
    @Enumerated(EnumType.STRING) @Builder.Default private OrderStatus status=OrderStatus.PENDING;
    @Column(name="total_amount",precision=12,scale=2) private BigDecimal totalAmount;
    @Column(name="delivery_address",columnDefinition="TEXT") private String deliveryAddress;
    @Builder.Default private String paymentMethod="COD";
    private String notes;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","password","tenant"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="consumer_id") private User consumer;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="farmer_tenant_id") private Tenant farmerTenant;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","order"})
    @OneToMany(mappedBy="order",cascade=CascadeType.ALL,fetch=FetchType.EAGER) private List<OrderItem> items;

    @CreationTimestamp @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name="updated_at") private LocalDateTime updatedAt;
}
