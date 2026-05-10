package com.agrosphere.entity;
import com.agrosphere.enums.DiseaseSeverity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="crop_disease_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CropDiseaseLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="crop_id") private Crop crop;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="disease_id") private Disease disease;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="tenant_id") private Tenant tenant;

    @Enumerated(EnumType.STRING) private DiseaseSeverity severity;

    @Column(name="detected_date") private LocalDate detectedDate;
    @Column(name="resolved_date") private LocalDate resolvedDate;
    @Column(columnDefinition="TEXT") private String notes;
    @Column(columnDefinition="TEXT") private String treatmentApplied;
    @Column(name="photo_url") private String photoUrl;

    @Column(name="is_resolved") @Builder.Default private Boolean isResolved = false;

    @CreationTimestamp @Column(name="created_at", updatable=false) private LocalDateTime createdAt;
}
