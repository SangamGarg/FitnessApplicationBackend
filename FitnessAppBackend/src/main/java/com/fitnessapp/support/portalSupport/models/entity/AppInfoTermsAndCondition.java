package com.fitnessapp.support.portalSupport.models.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_terms_conditions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoTermsAndCondition extends BaseContentEntity {

    @Id
    private Long id;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
}