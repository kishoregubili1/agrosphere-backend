package com.agrosphere.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ActivityRequest {
    private String title;
    private String notes;
    private LocalDate scheduledDate;
    private String activityType;
    private Long cropId;
    private Long templateId;
}
