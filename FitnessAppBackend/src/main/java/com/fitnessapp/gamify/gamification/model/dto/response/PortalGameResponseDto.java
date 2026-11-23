package com.fitnessapp.gamify.gamification.model.dto.response;

import com.fitnessapp.gamify.gamification.model.enums.DurationUnit;
import com.fitnessapp.gamify.gamification.model.enums.GameLevel;
import com.fitnessapp.gamify.gamification.model.enums.GameType;
import com.fitnessapp.gamify.gamification.model.entity.GameCatalogueEntity;
import com.fitnessapp.gamify.gamification.model.entity.GameDetailEntity;

import java.time.Instant;
import java.util.List;

public record PortalGameResponseDto(
        Long id,
        GameType gameType,
        String title,
        String subTitle,
        String description,
        String imageUrl,
        GameLevel level,
        Integer totalRepsRequired,
        int durationValue,
        DurationUnit durationUnit,
        boolean paid,
        long entryFeeCoins,
        long rewardCoins,
        long rewardXp,
        int maxRepsPerMinute,
        Instant releasedAt,
        int displayOrder,
        boolean active,
        String gifUrl,
        String videoUrl,
        List<String> rules,
        List<String> tips,
        Instant createdAt) {

    public static PortalGameResponseDto from(GameCatalogueEntity catalogue, GameDetailEntity detail) {
        return new PortalGameResponseDto(
                catalogue.getId(), catalogue.getGameType(), catalogue.getTitle(), catalogue.getSubTitle(),
                catalogue.getDescription(), catalogue.getImageUrl(), catalogue.getLevel(),
                catalogue.getTotalRepsRequired(), catalogue.getDurationValue(), catalogue.getDurationUnit(),
                catalogue.isPaid(), catalogue.getEntryFeeCoins(), catalogue.getRewardCoins(),
                catalogue.getRewardXp(), catalogue.getMaxRepsPerMinute(), catalogue.getReleasedAt(),
                catalogue.getDisplayOrder(), catalogue.isActive(),
                detail == null ? null : detail.getGifUrl(),
                detail == null ? null : detail.getVideoUrl(),
                detail == null ? List.of() : List.copyOf(detail.getRules()),
                detail == null ? List.of() : List.copyOf(detail.getTips()),
                catalogue.getCreatedAt());
    }
}
