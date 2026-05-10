package com.agrosphere.entity;
import com.agrosphere.enums.ActivityStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name="crop_activities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CropActivity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="crop_id") private Crop crop;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="tenant_id") private Tenant tenant;

    @Column(name="activity_type") private String activityType;
    private String title;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="scheduled_date") private LocalDate scheduledDate;
    @Column(name="completed_date") private LocalDate completedDate;
    @Enumerated(EnumType.STRING) @Builder.Default private ActivityStatus status = ActivityStatus.PENDING;
    private String notes;
}
