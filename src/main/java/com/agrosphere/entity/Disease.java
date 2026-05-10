package com.agrosphere.entity;

import com.agrosphere.enums.DiseaseSeverity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="diseases")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Disease {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false) private String name;
    @Column(columnDefinition="TEXT") private String symptoms;
    @Column(columnDefinition="TEXT") private String treatment;
    @Column(columnDefinition="TEXT") private String prevention;
    @Enumerated(EnumType.STRING) private DiseaseSeverity severity;
    private String affectedCrops;
    private String imageUrl;
    @Builder.Default private Boolean isActive = true;
}
