package com.fitnessapp.portal.audit.model;

import java.time.Instant;

public record PortalAuditResponseDto(
        Long id,
        String actorEmail,
        String action,
        String resourceType,
        String resourceId,
        String details,
        Instant createdAt
) {
    public static PortalAuditResponseDto from(PortalAuditEventEntity event) {
        return new PortalAuditResponseDto(
                event.getId(),
                event.getActorEmail(),
                event.getAction(),
                event.getResourceType(),
                event.getResourceId(),
                event.getDetails(),
                event.getCreatedAt()
        );
    }
}
