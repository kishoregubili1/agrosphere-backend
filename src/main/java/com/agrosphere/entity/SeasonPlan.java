package com.agrosphere.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name="season_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeasonPlan {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="tenant_id") private Tenant tenant;

    @Column(nullable=false) private String season;
    @Column(nullable=false) private Integer year;
    @Column(name="crops_json",columnDefinition="TEXT") private String cropsJson;
    @Column(length=2000) private String notes;
    @Builder.Default private String status = "planned";
    @CreationTimestamp @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
}
