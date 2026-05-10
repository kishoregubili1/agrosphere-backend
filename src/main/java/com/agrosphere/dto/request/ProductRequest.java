package com.agrosphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank  private String name;
    private String description;
    @NotNull   private BigDecimal price;
    @NotBlank  private String unit;
    private Integer stockQuantity;
    private String category;
    private String imageUrl;
    private Long cropId;
}
