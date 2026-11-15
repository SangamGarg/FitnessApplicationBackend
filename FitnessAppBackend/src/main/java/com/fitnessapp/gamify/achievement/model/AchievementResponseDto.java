package com.fitnessapp.gamify.achievement.model;

import java.time.Instant;
public record AchievementResponseDto(AchievementCode code, String title, String description, long rewardXp, long rewardCoins, boolean unlocked, Instant unlockedAt){}
