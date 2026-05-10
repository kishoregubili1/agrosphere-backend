package com.agrosphere.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="labour_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabourLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="tenant_id") private Tenant tenant;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","tenant"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="worker_id") private LabourWorker worker;

    @Column(name="work_type") private String workType;
    @Column(name="work_date") private LocalDate workDate;
    @Column(name="hours_worked") @Builder.Default private Integer hoursWorked = 8;
    @Column(name="days_worked")  @Builder.Default private Integer daysWorked  = 1;
    @Builder.Default private Boolean paid = false;
    @Column(name="paid_amount") private Double paidAmount;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","tenant","field"})
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="crop_id") private Crop crop;
    @Column(length=1000) private String notes;
    @CreationTimestamp @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
}
