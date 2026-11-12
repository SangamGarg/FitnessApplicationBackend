package com.fitnessapp.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthSnapshotDto {

    private String title;
    private String subtitle;

    private List<HealthMetricDto> metrics;

    private LocalDateTime lastUpdatedAt;
}
