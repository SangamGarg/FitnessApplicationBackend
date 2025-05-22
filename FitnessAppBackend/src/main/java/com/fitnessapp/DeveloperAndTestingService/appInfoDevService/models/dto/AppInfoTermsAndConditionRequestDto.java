package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AppInfoTermsAndConditionRequestDto {
    @NotBlank(message = "Content cannot be null or blank")
    private String content;

    private String lastUpdatedBy;

}
