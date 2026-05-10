package com.agrosphere.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="fields")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Field {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="farm_id") private Farm farm;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="tenant_id") private Tenant tenant;

    @Column(nullable=false) private String name;
    @Column(name="area_acres") private Double areaAcres;
    private String soilType;
    private String irrigationType;
    private String description;
}
