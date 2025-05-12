package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class AppInfoTermsAndCondition {
    @Lob
    private String content;
}
