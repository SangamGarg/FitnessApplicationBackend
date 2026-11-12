package com.fitnessapp.analytics.model.dtos.responseDto;

import com.fitnessapp.analytics.model.HealthSnapshotDto;
import com.fitnessapp.analytics.model.MeasureOthersDto;
import com.fitnessapp.analytics.model.PopularCalculatorDto;
import com.fitnessapp.analytics.model.WeightAnalyticsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsHomeResponseDto {

    private String title;
    private String subtitle;
    private String description;

    private HealthSnapshotDto healthSnapshot;
    private WeightAnalyticsDto weightAnalytics;
    private MeasureOthersDto measureOthers;

    private List<PopularCalculatorDto> popularCalculators;
}
