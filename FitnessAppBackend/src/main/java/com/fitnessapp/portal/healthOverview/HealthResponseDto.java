package com.fitnessapp.portal.healthOverview;

import java.time.Instant;

public record HealthResponseDto(
        String status,
        Instant checkedAt,
        long uptimeMs,
        DatabaseHealthDto database,
        JvmHealthDto jvm
) {
    public record DatabaseHealthDto(
            String status,
            long latencyMs,
            String database,
            ConnectionPoolDto connectionPool
    ) {}

    public record ConnectionPoolDto(
            String name,
            int active,
            int idle,
            int total,
            int maximum,
            int minimumIdle
    ) {}

    public record JvmHealthDto(
            long usedMemoryBytes,
            long maxMemoryBytes,
            int availableProcessors
    ) {}
}
