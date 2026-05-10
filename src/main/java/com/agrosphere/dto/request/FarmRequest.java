package com.agrosphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FarmRequest {
    @NotBlank
    private String name;
    private String location;
    private String district;
    private String state;
    private Double totalAreaAcres;
    private String description;
    private Double latitude;
    private Double longitude;
}
