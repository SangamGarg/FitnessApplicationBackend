package com.fitnessapp.analytics.model.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculatorCatalogItemResponseDto {
    private String key;
    private String name;
    private String category;
    private String description;
    private List<String> requiredInputs;
    private String unit;
    private boolean requiresSavedData;
    private boolean popular;
    private Integer displayOrder;
}
