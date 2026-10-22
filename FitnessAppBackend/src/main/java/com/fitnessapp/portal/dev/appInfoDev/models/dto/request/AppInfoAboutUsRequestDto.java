package com.fitnessapp.portal.dev.appInfoDev.models.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoAboutUsRequestDto {

    @NotBlank(message = "About Us content cannot be blank")
    @Schema(description = "About us description or rich HTML/text content", example = "Welcome to Fitness App...")
    private String content;
}