package com.agrosphere.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="tenant_id") private Tenant tenant;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="crop_id") private Crop crop;

    @Column(nullable=false) private String name;
    @Column(columnDefinition="TEXT") private String description;
    @Column(nullable=false, precision=10, scale=2) private BigDecimal price;
    @Column(nullable=false) private String unit;
    @Column(name="stock_quantity") @Builder.Default private Integer stockQuantity = 0;
    private String category;
    private String imageUrl;
    @Column(name="rating_avg") @Builder.Default private Double ratingAvg = 0.0;
    @Builder.Default private Boolean isActive = true;
    @CreationTimestamp @Column(name="created_at", updatable=false) private LocalDateTime createdAt;
}
