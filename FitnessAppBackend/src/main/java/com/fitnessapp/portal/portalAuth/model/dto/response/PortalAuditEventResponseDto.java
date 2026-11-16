package com.fitnessapp.portal.portalAuth.model.dto.response;

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
