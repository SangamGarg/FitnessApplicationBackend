package com.fitnessapp.profile.model.dto.response;

import com.fitnessapp.profile.model.entity.BadgeEntity;

import java.time.Instant;

public record BadgeResponseDto(
        Long id,
        String name,
        String description,
        String imageUrl,
        String category,
        boolean unlocked,
        Instant unlockedAt,
        boolean active
) {
    public static BadgeResponseDto from(BadgeEntity badge, Instant unlockedAt) {
        return new BadgeResponseDto(
                badge.getId(),
                badge.getName(),
                badge.getDescription(),
                badge.getImageUrl(),
                badge.getCategory(),
                unlockedAt != null,
                unlockedAt,
                badge.isActive()
        );
    }
}
