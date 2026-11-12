package com.fitnessapp.analytics.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularCalculatorDto {

    private String key;
    private String name;
    private String category;
    private String description;
    private String unit;
    private String iconKey;
    private boolean requiresSavedData;
}
