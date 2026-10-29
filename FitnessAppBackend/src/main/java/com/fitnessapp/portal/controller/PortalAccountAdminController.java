package com.fitnessapp.portal.controller;

import com.fitnessapp.portal.audit.PortalAuditService;
import com.fitnessapp.portal.audit.PortalAuditEventEntity;
import com.fitnessapp.portal.common.enums.UserRole;
import com.fitnessapp.portal.dto.EnabledRequestDto;
import com.fitnessapp.portal.dto.PortalAccountResponseDto;
import com.fitnessapp.portal.dto.PortalAuditEventResponseDto;
import com.fitnessapp.portal.dto.UpdatePortalAccountRequestDto;
import com.fitnessapp.portal.service.PortalAccountAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Set;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_BASE_URL;

@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_BASE_URL + "/admin")
@Tag(name = "Admin - Portal Accounts", description = "Search and manage portal accounts and review administrative audit events")
@SecurityRequirement(name = "bearerAuth")
public class PortalAccountAdminController {

    private static final Set<String> SORT_FIELDS = Set.of("createdAt", "updatedAt", "email", "fullName");

    private final PortalAccountAdminService accountService;
    private final PortalAuditService auditService;

    @GetMapping("/accounts")
    @Operation(summary = "Search portal accounts with filters and pagination")
    public ResponseEntity<Page<PortalAccountResponseDto>> listAccounts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Pageable pageable = pageable(page, size, sort, direction);
        return ResponseEntity.ok(accountService.list(query, role, enabled, pageable));
    }

    @GetMapping("/accounts/{id}")
    @Operation(summary = "Get a portal account without exposing credentials")
    public ResponseEntity<PortalAccountResponseDto> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.get(id));
    }

    @PatchMapping("/developers/{id}")
    @Operation(summary = "Update developer profile fields")
    public ResponseEntity<PortalAccountResponseDto> updateDeveloper(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePortalAccountRequestDto request
    ) {
        return ResponseEntity.ok(accountService.updateDeveloper(id, request));
    }


    //TODO Change the mapping
    @PatchMapping("/developers/fake/{id}/status")
    @Operation(summary = "Activate or suspend a developer account")
    public ResponseEntity<PortalAccountResponseDto> setDeveloperStatus(
            @PathVariable Long id,
            @Valid @RequestBody EnabledRequestDto request
    ) {
        return ResponseEntity.ok(accountService.setDeveloperEnabled(id, request.enabled()));
    }

    @GetMapping("/audit-events")
    @Operation(summary = "List administrative audit events")
    public ResponseEntity<Page<PortalAuditEventResponseDto>> listAuditEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PortalAuditEventResponseDto> result = auditService.list(pageable).map(this::toAuditResponse);
        return ResponseEntity.ok(result);
    }

    private Pageable pageable(int page, int size, String sort, String direction) {
        if (page < 0 || size < 1 || size > 100) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }
        String sortField = sort == null ? "" : sort.trim();
        if (!SORT_FIELDS.contains(sortField)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Unsupported sort field"
            );
        }
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Direction must be asc or desc"
            );
        }
        return PageRequest.of(page, size, Sort.by(sortDirection, sortField));
    }

    private PortalAuditEventResponseDto toAuditResponse(PortalAuditEventEntity event) {
        return new PortalAuditEventResponseDto(
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
