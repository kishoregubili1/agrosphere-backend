package com.agrosphere.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name="crop_growth_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CropGrowthLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="tenant_id") private Tenant tenant;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","tenant","field"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="crop_id") private Crop crop;

    @Column(nullable=false) private String stage;
    @Column(name="health_score") private Integer healthScore;
    @Column(length=2000) private String notes;
    @Column(name="issues_json",columnDefinition="TEXT") private String issuesJson;
    @Column(name="photo_url") private String photoUrl;
    @CreationTimestamp @Column(name="logged_at",updatable=false) private LocalDateTime loggedAt;
}
