package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Content cannot be null or blank")
    private String content;

    private String lastUpdatedBy;

}
