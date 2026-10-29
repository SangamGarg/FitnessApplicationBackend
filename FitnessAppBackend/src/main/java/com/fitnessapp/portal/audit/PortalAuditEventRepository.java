package com.fitnessapp.portal.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortalAuditEventRepository extends JpaRepository<PortalAuditEventEntity, Long> {
}
