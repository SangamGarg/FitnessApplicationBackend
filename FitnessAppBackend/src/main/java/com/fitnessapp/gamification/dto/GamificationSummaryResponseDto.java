package com.fitnessapp.gamification.dto;
import java.time.LocalDate;
public record GamificationSummaryResponseDto(long totalXp,int level,long coins,int currentStreak,int longestStreak,LocalDate lastActiveDate,long xpToNextLevel) {}
