package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AppInfoPrivacyPolicyRequestDto {
    private String content;
}
