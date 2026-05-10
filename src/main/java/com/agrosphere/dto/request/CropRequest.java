package com.agrosphere.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CropRequest {
    private String name;
    private Long cropTypeId;
    private Long farmId;      // farmer sends farmId, service maps to field/tenant
    private Long fieldId;     // optional direct field
    private String fieldName; // optional field name
    private LocalDate plantingDate;
    private LocalDate plantedDate;  // alias for compatibility
    private LocalDate expectedHarvestDate;
    private Double areaAcres;
    private String growthStage;
    private String currentGrowthStage; // alias
    private Integer healthScore;
    private String notes;
    private String variety;
    private String imageUrl;
}
