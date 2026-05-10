package com.agrosphere.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="order_id") private Order order;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","tenant","crop"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="product_id") private Product product;

    private Integer quantity;
    @Column(precision=10,scale=2) private BigDecimal price;
}
