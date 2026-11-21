package com.fitnessapp.profile.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "app_profile_badges", indexes = {
        @Index(name = "idx_profile_badge_active", columnList = "active"),
        @Index(name = "idx_profile_badge_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
public class BadgeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "text")
    private String criteriaMetadata;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public BadgeEntity(String name, String description, String imageUrl, String category) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
    }
}
