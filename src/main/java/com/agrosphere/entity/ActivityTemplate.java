package com.agrosphere.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="activity_templates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityTemplate {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="crop_type_id") private CropType cropType;
    private String activityType;
    private String name;
    private String title;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="day_from_planting") private Integer dayFromPlanting;
    @Builder.Default private Boolean isActive = true;
}
