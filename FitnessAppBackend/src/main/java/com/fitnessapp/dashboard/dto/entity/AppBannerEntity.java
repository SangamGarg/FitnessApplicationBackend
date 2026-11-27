package com.fitnessapp.dashboard.dto.entity;

import com.fitnessapp.dashboard.dto.BannerActionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "app_image_banners")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppBannerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String bannerKey;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 250)
    private String subTitle;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String imageStorageKey;

    @Column(length = 500)
    private String mobileImageUrl;

    @Column(length = 500)
    private String mobileImageStorageKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BannerActionType actionType;

    @Column(length = 500)
    private String actionValue;

    @Column(length = 150)
    private String actionLabel;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private boolean active;

    private Instant startAt;

    private Instant endAt;

    @Column(nullable = false)
    private boolean dismissible;

    @Column(nullable = false)
    private boolean trackingEnabled;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}