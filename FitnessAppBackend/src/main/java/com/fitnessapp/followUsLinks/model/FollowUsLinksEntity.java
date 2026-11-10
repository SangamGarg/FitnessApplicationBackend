package com.fitnessapp.followUsLinks.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "app_follow_us_links",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_follow_us_title_key",
                        columnNames = "title_key"
                ),
                @UniqueConstraint(
                        name = "uk_follow_us_normalized_url",
                        columnNames = "normalized_url"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FollowUsLinksEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 60)
    private String subTitle;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "title_key", nullable = false, length = 120)
    private String titleKey;

    @Column(name = "icon_key", nullable = false, length = 10)
    private String iconKey;

    @Column(name = "normalized_url", nullable = false, length = 600)
    private String normalizedUrl;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "updated_by", length = 150)
    private String updatedBy;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}