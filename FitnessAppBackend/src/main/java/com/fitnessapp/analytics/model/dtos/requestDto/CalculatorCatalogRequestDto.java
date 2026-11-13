package com.fitnessapp.analytics.model.dtos.requestDto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculatorCatalogRequestDto {
    private String key;
    private String name;
    private String category;
    private String description;
    private List<String> requiredInputs;
    private String unit;
    private Boolean requiresSavedData;
    private boolean popular;
    private Integer displayOrder;
}
