package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_info")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "content", column = @Column(name = "about_us_content"))
    private AppInfoAboutUs appInfoAboutUs;
    @Embedded
    @AttributeOverride(name = "content", column = @Column(name = "terms_and_condition_content"))
    private AppInfoTermsAndCondition appInfoTermsAndCondition;
    @Embedded
    @AttributeOverride(name = "content", column = @Column(name = "privacy_policy_content"))
    private AppInfoPrivacyPolicy appInfoPrivacyPolicy;
//
//    private String lastUpdatedBy;
//
//    private String version;
//
//    private LocalDateTime createdAt;
//
//    private LocalDateTime updatedAt;

}