package com.fitnessapp.gamify.gamification.model.dto.response;

import com.fitnessapp.gamify.gamification.model.enums.DurationUnit;
import com.fitnessapp.gamify.gamification.model.enums.GameLevel;
import com.fitnessapp.gamify.gamification.model.enums.GameType;
import com.fitnessapp.gamify.gamification.model.entity.GameCatalogueEntity;

import java.time.Instant;

public record GameCatalogueResponseDto(
        GameType gameType, String imageUrl, String title, String subTitle, String description,
        GameLevel level,
        Integer totalRepsRequired,
        int durationValue, DurationUnit durationUnit, String durationLabel,   // 1, MINUTE, "1 minute"
        boolean paid, long entryFeeCoins,                                     // coins required to play
        long rewardCoins,                                                     // coins you can win
        long rewardXp,                                                        // XP you can earn
        boolean isNew) {

    public static GameCatalogueResponseDto from(GameCatalogueEntity e) {
        return from(e, Instant.now());
    }

    public static GameCatalogueResponseDto from(GameCatalogueEntity e, Instant now) {
        return new GameCatalogueResponseDto(
                e.getGameType(), e.getImageUrl(), e.getTitle(), e.getSubTitle(), e.getDescription(),
                e.getLevel(), e.getTotalRepsRequired(),
                e.getDurationValue(), e.getDurationUnit(), e.getDurationUnit().format(e.getDurationValue()),
                e.isPaid(), e.getEntryFeeCoins(), e.getRewardCoins(), e.getRewardXp(), e.isNewGame(now));
    }
}