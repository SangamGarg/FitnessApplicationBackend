package com.fitnessapp.gamify.gamification.model.dto.request;

import com.fitnessapp.gamify.gamification.model.enums.DurationUnit;
import com.fitnessapp.gamify.gamification.model.enums.GameLevel;
import com.fitnessapp.gamify.gamification.model.enums.GameType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record CreateGameRequestDto(
        @NotNull GameType gameType,
        @NotBlank @Size(max = 120) String title,
        @Size(max = 200) String subTitle,
        @NotBlank @Size(max = 1000) String description,
        @NotBlank @Size(max = 500) String imageUrl,
        @NotNull GameLevel level,
        Integer totalRepsRequired,
        @Min(1) int durationValue,
        @NotNull DurationUnit durationUnit,
        boolean paid,
        long entryFeeCoins,
        long rewardCoins,
        long rewardXp,
        int maxRepsPerMinute,
        Instant releasedAt,
        int displayOrder,

        // Details fields
        @Size(max = 500) String gifUrl,
        @Size(max = 500) String videoUrl,
        List<String> rules,
        List<String> tips
) {
}
