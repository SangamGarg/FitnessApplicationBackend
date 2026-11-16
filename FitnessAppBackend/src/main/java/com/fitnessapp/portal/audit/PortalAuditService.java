package com.fitnessapp.portal.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PortalAuditService {

    private final PortalAuditEventRepository events;

    @Transactional
    public void record(String action, String resourceType, String resourceId, String details) {
        events.save(new PortalAuditEventEntity(
                getCurrentUserPortal(),
                action,
                resourceType,
                resourceId,
                details
        ));
    }
    public String getCurrentUserPortal() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {

            throw new IllegalStateException(
                    "No authenticated user found"
            );
        }

        return authentication.getName();
    }

    @Transactional(readOnly = true)
    public Page<PortalAuditEventEntity> list(Pageable pageable) {
        return events.findAll(pageable);
    }
}
