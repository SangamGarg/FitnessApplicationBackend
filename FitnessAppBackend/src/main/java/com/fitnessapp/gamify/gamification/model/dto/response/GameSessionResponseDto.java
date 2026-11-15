package com.fitnessapp.gamify.gamification.model.dto.response;

import com.fitnessapp.gamify.gamification.model.entity.GameSessionEntity;
import com.fitnessapp.gamify.gamification.model.enums.GameSessionStatus;
import com.fitnessapp.gamify.gamification.model.enums.GameType;

import java.time.Instant;
import java.util.UUID;

public record GameSessionResponseDto(UUID sessionId, GameType gameType, GameSessionStatus status, Instant startedAt,
                                     Instant completedAt, Integer repsDetected, Integer holdSeconds,
                                     Integer durationSeconds, Long score, String trackerVersion,
                                     Double modelConfidence) {
    public static GameSessionResponseDto from(GameSessionEntity e) {
        return new GameSessionResponseDto(e.getId(), e.getGameType(), e.getStatus(), e.getStartedAt(), e.getCompletedAt(), e.getRepsDetected(), e.getHoldSeconds(), e.getDurationSeconds(), e.getScore(), e.getTrackerVersion(), e.getModelConfidence());
    }
}
