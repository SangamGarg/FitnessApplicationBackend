package com.fitnessapp.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthMetricDto {

    private String key;

    private String title;

    private String value;

    private String unit;

    private String status;

    private String iconKey;

    private Integer displayOrder;
}