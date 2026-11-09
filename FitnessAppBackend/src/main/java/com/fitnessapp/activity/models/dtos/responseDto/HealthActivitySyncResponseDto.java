package com.fitnessapp.activity.models.dtos.responseDto;

public record HealthActivitySyncResponseDto(
        int receivedRecords,
        int importedRecords,
        int updatedRecords,
        int duplicateRecords,
        int ignoredOwnAppRecords
) {
}
