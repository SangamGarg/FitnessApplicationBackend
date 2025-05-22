package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "app_info")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_info_id")
    private Long id;

    @ElementCollection
    @CollectionTable(name = "app_links", joinColumns = @JoinColumn(name = "app_info_id"))
    private List<AppInfoLinks> appInfoLinks;

    @ElementCollection
    @CollectionTable(name = "app_faqs", joinColumns = @JoinColumn(name = "app_info_id"))
    private List<AppInfoFaqs> appInfoFaqs;

    @Embedded
    @AttributeOverride(name = "content", column = @Column(name = "about_us_content"))
    private AppInfoAboutUs appInfoAboutUs;
    @Embedded
    @AttributeOverride(name = "content", column = @Column(name = "terms_and_condition_content"))
    private AppInfoTermsAndCondition appInfoTermsAndCondition;
    @Embedded
    @AttributeOverride(name = "content", column = @Column(name = "privacy_policy_content"))
    private AppInfoPrivacyPolicy appInfoPrivacyPolicy;


    private String createdAt;

    @Column(name = "version")
    private String version;
}