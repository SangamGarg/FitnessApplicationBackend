package com.fitnessapp.portal.healthOverview;

import java.time.Instant;

public record ApiLatencyDto(
        Instant measuredAt,
        long requestCount,
        double averageLatencyMs,
        long maximumLatencyMs
) {}
