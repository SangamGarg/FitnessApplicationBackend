package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AppInfoAboutUsRequestDto {
    private String content;
}
