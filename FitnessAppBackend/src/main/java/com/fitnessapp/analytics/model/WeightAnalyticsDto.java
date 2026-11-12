package com.fitnessapp.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightAnalyticsDto {

    private String title;
    private String subtitle;

    private String metric;
    private String unit;

    private String period;
    private List<WeightTrendPointDto> trend;

    private List<AnalyticsInsightDto> insights;
}
