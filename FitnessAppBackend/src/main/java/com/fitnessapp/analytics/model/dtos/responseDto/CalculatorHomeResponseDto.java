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
public class CalculatorHomeResponseDto {

    private String title;
    private String subtitle;
    private String description;
    private List<CalculatorCategoryDto> calculatorCategories;
    private List<PopularCalculatorDto> popularCalculators;
    private List<PopularCalculatorDto> recentlyUsedCalculators;
}
