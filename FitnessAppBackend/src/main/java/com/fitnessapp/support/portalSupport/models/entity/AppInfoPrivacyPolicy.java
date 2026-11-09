package com.fitnessapp.support.portalSupport.models.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_privacy_policy")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoPrivacyPolicy extends BaseContentEntity {

    @Id
    private Long id;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
}