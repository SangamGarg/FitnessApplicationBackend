package com.fitnessapp.analytics.model.dtos.responseDto;

import com.fitnessapp.analytics.model.PopularCalculatorDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculatorCategoryDto {
    private String category;
    private List<PopularCalculatorDto> calculators;
}
