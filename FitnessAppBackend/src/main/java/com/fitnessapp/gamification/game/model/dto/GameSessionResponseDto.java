package com.fitnessapp.gamification.game.model.dto;
import com.fitnessapp.gamification.game.model.GameSessionEntity;
import com.fitnessapp.gamification.game.model.GameSessionStatus;
import com.fitnessapp.gamification.game.model.GameType;

import java.time.Instant; import java.util.UUID;
public record GameSessionResponseDto(UUID sessionId, GameType gameType, GameSessionStatus status, Instant startedAt, Instant completedAt, Integer repsDetected, Integer holdSeconds, Integer durationSeconds, Long score, String trackerVersion, Double modelConfidence) { public static GameSessionResponseDto from(GameSessionEntity e){return new GameSessionResponseDto(e.getId(),e.getGameType(),e.getStatus(),e.getStartedAt(),e.getCompletedAt(),e.getRepsDetected(),e.getHoldSeconds(),e.getDurationSeconds(),e.getScore(),e.getTrackerVersion(),e.getModelConfidence());}}
