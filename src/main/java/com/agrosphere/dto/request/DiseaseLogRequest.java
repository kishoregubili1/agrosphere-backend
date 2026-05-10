package com.agrosphere.dto.request;

import com.agrosphere.enums.DiseaseSeverity;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DiseaseLogRequest {
    private Long cropId;
    private Long diseaseId;
    private LocalDate detectedDate;
    private String notes;
    private DiseaseSeverity severity;
    private String photoUrl;
}
