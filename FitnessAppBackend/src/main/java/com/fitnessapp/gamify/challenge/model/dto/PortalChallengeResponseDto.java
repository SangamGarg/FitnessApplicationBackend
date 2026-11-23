package com.fitnessapp.gamify.challenge.model.dto;

import com.fitnessapp.gamify.challenge.model.enums.ChallengeMetric;

import java.time.Instant;

public record PortalChallengeResponseDto(
        Long id,
        String title,
        String description,
        ChallengeMetric metric,
        long targetValue,
        String activityType,
        Instant startsAt,
        Instant endsAt,
        long rewardXp,
        long rewardCoins,
        boolean active,
        Instant createdAt) {
}
