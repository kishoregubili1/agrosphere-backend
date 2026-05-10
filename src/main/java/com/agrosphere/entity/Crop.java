package com.agrosphere.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="crops")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Crop {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","crops","users"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="tenant_id") private Tenant tenant;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="crop_type_id") private CropType cropType;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","tenant","farm"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="field_id") private Field field;

    @Column(nullable=true) private String name;
    private String variety;
    @Column(name="area_acres") private Double areaAcres;
    @Column(name="planting_date") private LocalDate plantingDate;
    @Column(name="expected_harvest_date") private LocalDate expectedHarvestDate;
    @Column(name="current_growth_stage") private String currentGrowthStage;
    @Column(name="health_score") @Builder.Default private Integer healthScore = 85;
    private String imageUrl;
    private String notes;
    @CreationTimestamp @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
}
