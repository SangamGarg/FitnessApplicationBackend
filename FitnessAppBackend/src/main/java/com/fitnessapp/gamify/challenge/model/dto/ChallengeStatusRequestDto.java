package com.fitnessapp.gamify.challenge.model.dto;

import jakarta.validation.constraints.NotNull;

public record ChallengeStatusRequestDto(@NotNull Boolean active) {
}
