package com.fitnessapp.profile.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileHomeResponseDto {
    private String name;
    private String email;
    private String profileImageUrl;
    private long totalCoins;
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastActiveDate;
    private XpProgressDto xp;
    private List<BadgeResponseDto> badges;
    private List<ProgressGalleryResponseDto> progressGallery;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class XpProgressDto {
        private int currentLevel;
        private long currentXp;
        private long currentLevelRequiredXp;
        private int nextLevel;
        private long nextLevelRequiredXp;
        private long xpToNextLevel;
        private double progressPercentage;
    }
}
