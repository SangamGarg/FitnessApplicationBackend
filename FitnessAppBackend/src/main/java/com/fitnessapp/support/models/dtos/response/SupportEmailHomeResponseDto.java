package com.fitnessapp.support.models.dtos.response;

import com.fitnessapp.support.models.dtos.SupportEmailValidationDto;
import com.fitnessapp.support.models.dtos.SupportIssueCategoryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportEmailHomeResponseDto {

    private String title;
    private String subtitle;
    private String description;

    private List<SupportIssueCategoryDto> categories;

    private SupportEmailValidationDto validation;

    private boolean diagnosticsEnabled;
}