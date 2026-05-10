package com.agrosphere.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="inventory_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","crops","users"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="tenant_id") private Tenant tenant;

    @Column(nullable=false) private String name;
    @Column(nullable=false) private String category;
    @Column(nullable=false) private Double quantity;
    @Builder.Default private String unit = "kg";
    @Column(name="min_stock") @Builder.Default private Double minStock = 0.0;
    @Column(name="cost_per_unit") @Builder.Default private Double costPerUnit = 0.0;
    private String supplier;
    @Column(name="purchase_date") private LocalDate purchaseDate;
    @Column(name="expiry_date") private LocalDate expiryDate;
    @Column(length=1000) private String notes;
    @CreationTimestamp @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
}
