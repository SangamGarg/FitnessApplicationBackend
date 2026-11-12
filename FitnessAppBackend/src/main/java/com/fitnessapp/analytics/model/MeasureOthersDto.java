package com.fitnessapp.analytics.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeasureOthersDto {

    private String title;
    private String description;
    private String buttonText;
    private String action;
}
