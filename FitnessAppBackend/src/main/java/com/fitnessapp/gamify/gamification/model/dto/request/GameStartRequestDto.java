package com.fitnessapp.gamify.gamification.model.dto.request;

import com.fitnessapp.gamify.gamification.model.enums.GameType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GameStartRequestDto(@NotNull GameType gameType, @NotNull UUID clientRequestId) {
}
