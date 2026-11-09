package com.fitnessapp.support.portalSupport.models.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_about_us")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoAboutUs extends BaseContentEntity {

    @Id
    private Long id;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
}