package com.agrosphere.dto.request;

import com.agrosphere.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FinanceRequest {
    @NotBlank  private String title;
    private String description;
    @NotNull   private BigDecimal amount;
    @NotNull   private TransactionType type;
    private String category;
    private LocalDate transactionDate;
    private Long cropId; // optional
}
