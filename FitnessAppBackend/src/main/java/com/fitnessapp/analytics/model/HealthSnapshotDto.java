package com.fitnessapp.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthSnapshotDto {

    private BigDecimal bmi;
    private String bmiStatus;

    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private BigDecimal bodyFatPercentage;

    private LocalDateTime lastUpdatedAt;
}
