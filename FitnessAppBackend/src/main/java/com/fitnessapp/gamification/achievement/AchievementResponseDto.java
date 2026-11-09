package com.fitnessapp.gamification.achievement;
import java.time.Instant;
public record AchievementResponseDto(AchievementCode code,String title,String description,long rewardXp,long rewardCoins,boolean unlocked,Instant unlockedAt){}
