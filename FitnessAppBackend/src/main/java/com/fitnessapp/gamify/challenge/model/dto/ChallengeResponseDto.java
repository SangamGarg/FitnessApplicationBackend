package com.fitnessapp.gamify.challenge.model.dto;

import com.fitnessapp.gamify.challenge.model.enums.ChallengeMetric;
import com.fitnessapp.gamify.challenge.model.enums.ChallengeStatus;

import java.time.Instant;

public record ChallengeResponseDto(Long id, String title, String description, ChallengeMetric metric, long targetValue,
                                   long progressValue,
                                   ChallengeStatus status, Instant startsAt, Instant endsAt, long rewardXp,
                                   long rewardCoins, boolean joined) {
}
