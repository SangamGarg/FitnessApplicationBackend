package com.fitnessapp.gamification.challenge;

import jakarta.validation.constraints.*;

import java.time.Instant;

public record CreateChallengeRequestDto(@NotBlank @Size(max = 120) String title, @Size(max = 1000) String description,
                                        @NotNull ChallengeMetric metric, @Positive long targetValue,
                                        @Size(max = 40) String activityType, @NotNull Instant startsAt,
                                        @NotNull Instant endsAt, @Min(0) @Max(100000) long rewardXp,
                                        @Min(0) @Max(100000) long rewardCoins) {
}
