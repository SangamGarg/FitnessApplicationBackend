package com.fitnessapp.portal.dev.appInfoDev.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoTermsAndCondition {

    @Column(name = "terms_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "terms_last_updated")
    private String lastUpdatedBy;
}