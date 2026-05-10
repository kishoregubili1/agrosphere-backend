package com.agrosphere.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name="labour_workers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabourWorker {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="tenant_id") private Tenant tenant;

    @Column(nullable=false) private String name;
    private String phone;
    private String village;
    @Builder.Default private String skill = "General";
    @Column(name="daily_wage") private Double dailyWage;
    @Column(length=1000) private String notes;
    @Column(name="photo_url") private String photoUrl;
    @CreationTimestamp @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
}
