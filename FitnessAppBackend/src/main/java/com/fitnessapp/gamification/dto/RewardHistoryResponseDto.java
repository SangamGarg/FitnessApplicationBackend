package com.fitnessapp.gamification.dto;
import java.time.Instant;
import java.time.LocalDate;
public record RewardHistoryResponseDto(String sourceType,String sourceId,long xpAwarded,long coinsAwarded,LocalDate rewardDate,Instant createdAt) {}
