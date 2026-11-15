package com.fitnessapp.gamify.gamification.model.dto.response;

import java.time.LocalDate;
import java.util.List;

public record GamificationHomeSummaryResponseDto(


        String title,
        String subTitle,
        String description,
        int totalAvailableGames,
        List<GameCatalogueResponseDto> games,
        long totalXp, int level, long coins, int currentStreak, int longestStreak,
        LocalDate lastActiveDate, long xpToNextLevel) {
}
