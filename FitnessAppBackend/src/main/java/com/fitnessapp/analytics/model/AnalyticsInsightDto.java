package com.fitnessapp.analytics.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsInsightDto {

    private String key;
    private String title;
    private BigDecimal value;
    private String unit;

    // INCREASED, DECREASED, UNCHANGED
    private String trend;

    private String description;
}
