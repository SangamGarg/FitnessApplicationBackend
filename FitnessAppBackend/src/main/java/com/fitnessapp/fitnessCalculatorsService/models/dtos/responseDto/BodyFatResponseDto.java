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
public class BodyFatResponseDto {
    private String status;
    private Integer statusCode;
    private Map<String, Object> data;
    private String imageUrl;
    private String femaleUrl;
    private String maleUrl;
    private String aboutBodyFat;
}
