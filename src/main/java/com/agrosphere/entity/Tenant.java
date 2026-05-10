package com.agrosphere.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="tenants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tenant {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    private String email;
    private String phoneNumber;
    private String district;
    private String state;
    private String logoUrl;
    @Builder.Default private Boolean isActive=true;
}
