package com.fitnessapp.portal.audit.service;

import com.fitnessapp.portal.audit.model.PortalAuditEventEntity;
import com.fitnessapp.portal.audit.model.PortalAuditResponseDto;
import com.fitnessapp.portal.audit.repo.PortalAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PortalAuditService {

    private final PortalAuditEventRepository events;


    @Transactional(readOnly = true)
    public Page<PortalAuditResponseDto> getAuditForAdmin(
            String actorEmail,
            String action,
            String resourceType,
            String resourceId,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        Specification<PortalAuditEventEntity> filters = (root, query, cb) -> cb.conjunction();

        if (hasText(actorEmail)) {
            filters = filters.and((root, query, cb) -> cb.equal(
                    cb.lower(root.get("actorEmail")), actorEmail.trim().toLowerCase(Locale.ROOT)
            ));
        }
        if (hasText(action)) {
            filters = filters.and((root, query, cb) -> cb.equal(root.get("action"), action.trim()));
        }
        if (hasText(resourceType)) {
            filters = filters.and((root, query, cb) -> cb.equal(root.get("resourceType"), resourceType.trim()));
        }
        if (hasText(resourceId)) {
            filters = filters.and((root, query, cb) -> cb.equal(root.get("resourceId"), resourceId.trim()));
        }
        if (from != null) {
            filters = filters.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            filters = filters.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }

        return events.findAll(filters, pageable).map(PortalAuditResponseDto::from);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

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

}
