package com.fitnessapp.portal.dev.appInfoDev.models.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_info")
public class AppInfoEntity {

    @Id
    private Long id;

    @Column(name = "created_at", length = 30)
    private String createdAt;

    @Column(name = "app_version", length = 30)
    private String version;

    @Embedded
    private AppInfoAboutUs appInfoAboutUs;

    @Embedded
    private AppInfoPrivacyPolicy appInfoPrivacyPolicy;

    @Embedded
    private AppInfoTermsAndCondition appInfoTermsAndCondition;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "app_info_id")
    private List<AppInfoLinks> appInfoLinks = new ArrayList<>();

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "app_info_id")
    private List<AppInfoFaqs> appInfoFaqs = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppInfoEntity that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}