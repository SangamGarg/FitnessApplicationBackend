package com.fitnessapp.fitnessCalculatorsService.models.dtos.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdealWeightResponseDto {
    private String status;
    private Integer statusCode;
    private Map<String, Object> idealWeight;
    private String imageUrl;
    private String aboutIdealWeight;
}
