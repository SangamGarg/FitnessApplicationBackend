package com.fitnessapp.profile.model.entity;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "app_profile_user_progress_gallery")
@Getter
@Setter
@NoArgsConstructor
public class ProgressGalleryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @Column(length = 500)
    private String caption;

    @Column(length = 50)
    private String category;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    public ProgressGalleryEntity(UserEntity user, String imageUrl, String caption) {
        this.user = user;
        this.imageUrl = imageUrl;
        this.caption = caption;
    }
}
