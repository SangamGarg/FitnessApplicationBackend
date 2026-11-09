package com.fitnessapp.activity.models.dtos.responseDto;

import com.fitnessapp.activity.models.entities.ActivityRecordEntity;
import com.fitnessapp.activity.models.enums.ActivitySource;
import com.fitnessapp.activity.models.enums.ActivityStatus;
import com.fitnessapp.activity.models.enums.ActivityType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ActivityRecordResponseDto(
        UUID id,
        ActivitySource source,
        ActivityType activityType,
        ActivityStatus status,
        String activityName,
        Instant startedAt,
        Instant endedAt,
        Integer durationSeconds,
        BigDecimal caloriesBurned,
        BigDecimal distanceMeters,
        Long steps,
        boolean includedInSummary,
        UUID duplicateOfId
) {
    public static ActivityRecordResponseDto from(ActivityRecordEntity entity) {
        return new ActivityRecordResponseDto(
                entity.getId(),
                entity.getSource(),
                entity.getActivityType(),
                entity.getStatus(),
                entity.getActivityName(),
                entity.getStartedAt(),
                entity.getEndedAt(),
                entity.getDurationSeconds(),
                entity.getCaloriesBurned(),
                entity.getDistanceMeters(),
                entity.getSteps(),
                entity.isIncludedInSummary(),
                entity.getDuplicateOf() == null ? null : entity.getDuplicateOf().getId()
        );
    }
}
