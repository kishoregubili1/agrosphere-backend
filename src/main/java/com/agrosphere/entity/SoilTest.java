package com.agrosphere.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="soil_tests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SoilTest {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="tenant_id") private Tenant tenant;

    @Column(name="field_name",nullable=false) private String fieldName;
    @Column(name="soil_type") private String soilType;
    @Column(name="test_date") private LocalDate testDate;
    private Double ph;
    private Double nitrogen;
    private Double phosphorus;
    private Double potassium;
    @Column(name="organic_matter") private Double organicMatter;
    @Builder.Default private String zinc = "adequate";
    @Builder.Default private String boron = "adequate";
    @Column(length=2000) private String notes;
    @CreationTimestamp @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
}
