package com.fitnessapp.analytics.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightTrendPointDto {

    private LocalDate date;
    private String label;
    private BigDecimal value;
}
