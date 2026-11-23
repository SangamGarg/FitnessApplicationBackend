package com.fitnessapp.gamify.challenge.model.dto;

import com.fitnessapp.gamify.challenge.model.enums.ChallengeMetric;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record ChallengePatchRequestDto(
        @Size(max = 120) String title,
        @Size(max = 1000) String description,
        ChallengeMetric metric,
        @Positive Long targetValue,
        @Size(max = 40) String activityType,
        Instant startsAt,
        Instant endsAt,
        @Min(0) @Max(100000) Long rewardXp,
        @Min(0) @Max(100000) Long rewardCoins) {
}
