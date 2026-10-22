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
public class AppInfoTermsAndConditionRequestDto {

    @NotBlank(message = "Terms & Conditions content cannot be blank")
    @Schema(description = "Full text or markdown content of Terms and Conditions")
    private String content;
}