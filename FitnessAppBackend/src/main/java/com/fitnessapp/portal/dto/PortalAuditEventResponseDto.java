package com.fitnessapp.portal.dto;

import java.time.Instant;

public record PortalAuditEventResponseDto(
        Long id,
        String actorEmail,
        String action,
        String resourceType,
        String resourceId,
        String details,
        Instant createdAt
) {
}
