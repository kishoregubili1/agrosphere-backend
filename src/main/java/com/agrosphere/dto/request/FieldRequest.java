package com.agrosphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FieldRequest {
    @NotBlank private String name;
    private Double areaAcres;
    private String soilType;
    private String description;
    @NotNull private Long farmId;
}
