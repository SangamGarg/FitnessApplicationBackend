package com.fitnessapp.gamify.gamification.model.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record RewardHistoryResponseDto(String sourceType, String sourceId, long xpAwarded, long coinsAwarded,
                                       LocalDate rewardDate, Instant createdAt) {
}
