package com.fitnessapp.followUsLinks.model;

import java.time.LocalDateTime;

public record PortalFollowUsLinksResponseDto(
        Long id,
        String title,
        String subtitle,
        String url,
        String iconKey,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String updatedBy
) {
    public static PortalFollowUsLinksResponseDto from(FollowUsLinksEntity entity) {
        return new PortalFollowUsLinksResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getSubTitle(),
                entity.getUrl(),
                entity.getIconKey(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}