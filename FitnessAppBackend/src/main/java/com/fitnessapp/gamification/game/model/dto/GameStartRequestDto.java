package com.fitnessapp.gamification.game.model.dto;

import com.fitnessapp.gamification.game.model.GameType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GameStartRequestDto(@NotNull GameType gameType, @NotNull UUID clientRequestId) {
}
