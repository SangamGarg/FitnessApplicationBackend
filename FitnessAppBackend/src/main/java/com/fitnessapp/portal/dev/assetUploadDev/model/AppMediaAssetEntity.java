package com.fitnessapp.portal.dev.assetUploadDev.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_media_assets", indexes = {
        @Index(name = "idx_asset_key", columnList = "asset_key", unique = true),
        @Index(name = "idx_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppMediaAssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique machine-readable identifier used by clients (e.g. "ERR_NETWORK_TIMEOUT", "ABOUT_US_HERO")
    @Column(name = "asset_key", nullable = false, unique = true, length = 100)
    private String assetKey;

    // Human-readable title or label for administrative use
    @Column(nullable = false, length = 150)
    private String title;

    // Direct CDN / Supabase Storage / S3 URL where the asset is hosted
    @Column(name = "asset_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "asset_size_bytes")
    private Long fileSizeBytes;


    @Column(name = "updated_by_email", length = 254)
    private String updatedByEmail;

    @Column(name = "asset_extension")
    private String assetExtension;

    // Accessibility alt text to render in mobile ImageView / web <img>
    @Column(name = "alt_text")
    private String altText;

    // Logical grouping to fetch all images for a specific screen at once
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssetCategory category;

    // Optional descriptive note or subtitle (e.g. "Something went wrong on our end")
    @Column(columnDefinition = "TEXT")
    private String description;

    // Soft-disable switch so you can swap out images without deleting database rows
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}