package com.fitnessapp.support.portalSupportApp.models.dto.request;

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
    private String content;
}