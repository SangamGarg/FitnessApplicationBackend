package com.fitnessapp.portal.audit.repo;

import com.fitnessapp.portal.audit.model.PortalAuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PortalAuditEventRepository extends JpaRepository<PortalAuditEventEntity, Long>,
        JpaSpecificationExecutor<PortalAuditEventEntity> {
}
