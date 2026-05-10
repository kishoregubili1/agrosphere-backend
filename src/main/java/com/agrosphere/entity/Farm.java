package com.agrosphere.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="farms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Farm {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="tenant_id", nullable=false) private Tenant tenant;

    @Column(nullable=false) private String name;
    private String location;
    private String district;
    private String state;
    @Column(name="total_area_acres") private Double totalAreaAcres;
    private String description;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    @Builder.Default private Boolean isActive = true;
}
