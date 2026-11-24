package com.fitnessapp.portal.audit.controller;


import com.fitnessapp.portal.audit.model.PortalAuditResponseDto;
import com.fitnessapp.portal.audit.service.PortalAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_ADMIN_AUDIT_ENDPOINT;

@RestController
@Slf4j
@RequestMapping(PORTAL_ADMIN_AUDIT_ENDPOINT)
@RequiredArgsConstructor
public class PortalAuditController {

    private final PortalAuditService auditService;


    @GetMapping("/events")
    public ResponseEntity<Page<PortalAuditResponseDto>> getAuditForAdmin(
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.getAuditForAdmin(
                actorEmail, action, resourceType, resourceId, from, to, pageable
        ));
    }
}
