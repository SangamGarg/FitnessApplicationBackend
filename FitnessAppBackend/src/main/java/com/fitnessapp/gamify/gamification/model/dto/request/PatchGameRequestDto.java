package com.fitnessapp.gamify.gamification.model.dto.request;

import com.fitnessapp.gamify.gamification.model.enums.DurationUnit;
import com.fitnessapp.gamify.gamification.model.enums.GameLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record PatchGameRequestDto(
        @Size(max = 120) String title,
        @Size(max = 200) String subTitle,
        @Size(max = 1000) String description,
        GameLevel level,
        Integer totalRepsRequired,
        @Min(1) Integer durationValue,
        DurationUnit durationUnit,
        Boolean paid,
        Long entryFeeCoins,
        Long rewardCoins,
        Long rewardXp,
        Integer maxRepsPerMinute,
        Instant releasedAt,
        Integer displayOrder,
        Boolean active,
        List<String> rules,
        List<String> tips) {
}
