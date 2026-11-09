package com.fitnessapp.portal.audit;

import com.fitnessapp.portal.common.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PortalAuditService {

    private final PortalAuditEventRepository events;
    private final CurrentUserService currentUser;

    @Transactional
    public void record(String action, String resourceType, String resourceId, String details) {
        events.save(new PortalAuditEventEntity(
                currentUser.getEmail(),
                action,
                resourceType,
                resourceId,
                details
        ));
    }

    @Transactional(readOnly = true)
    public Page<PortalAuditEventEntity> list(Pageable pageable) {
        return events.findAll(pageable);
    }
}
