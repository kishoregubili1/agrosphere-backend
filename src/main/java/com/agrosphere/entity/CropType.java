package com.agrosphere.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="crop_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CropType {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false) private String name;
    private String category;
    private String seasonType;
    private Integer avgDurationDays;
    private String iconEmoji;
    private String imageUrl;
    private String description;
    @Builder.Default private Boolean isActive = true;
}
