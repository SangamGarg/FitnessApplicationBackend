package com.fitnessapp.fitnessCalculators.models.dtos.responseDto;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BmiCalculateResponseDto {
    private String status;
    private Integer statusCode;
    private Float bmi;
    private String healthStatus;
    private String healthy_bmi_range;
    private String imageUrl;
    private String aboutBmi;
}
