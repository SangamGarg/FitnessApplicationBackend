package com.fitnessapp.portal.portalAuth.controller;

import com.fitnessapp.portal.audit.model.PortalAuditEventEntity;
import com.fitnessapp.portal.portalAuth.model.dto.request.CreateDeveloperRequestDto;
import com.fitnessapp.portal.portalAuth.model.dto.request.EnabledRequestDto;
import com.fitnessapp.portal.portalAuth.model.dto.request.UpdatePortalAccountRequestDto;
import com.fitnessapp.portal.portalAuth.model.dto.response.PortalAccountResponseDto;
import com.fitnessapp.portal.portalAuth.model.dto.response.PortalAuditEventResponseDto;
import com.fitnessapp.portal.portalAuth.model.enums.UserRole;
import com.fitnessapp.portal.portalAuth.service.PortalAccountAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_ADMIN_BASE_ENDPOINT;

@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_ADMIN_BASE_ENDPOINT)
@Slf4j
public class PortalAdminController {

    private static final Set<String> SORT_FIELDS = Set.of("createdAt", "updatedAt", "email", "fullName");

    private final PortalAccountAdminService accountService;


    @GetMapping("/portal-accounts")
    public ResponseEntity<Page<PortalAccountResponseDto>> listPortalAccounts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Pageable pageable = pageable(page, size, sort, direction);
        return ResponseEntity.ok(accountService.listPortalAccounts(query, role, enabled, pageable));
    }

    @GetMapping("/portal-accounts/{id}")
    public ResponseEntity<PortalAccountResponseDto> getPortalAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getPortalAccount(id));
    }

    @PatchMapping("/developers/{id}")
    public ResponseEntity<PortalAccountResponseDto> updateDeveloper(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePortalAccountRequestDto request
    ) {
        return ResponseEntity.ok(accountService.updateDeveloperDetails(id, request));
    }

    @GetMapping("/developers")
    public ResponseEntity<List<Map<String, Object>>> listDevelopers() {
        log.debug("REST request to list all developer accounts");
        List<Map<String, Object>> result = accountService.listDevelopers();
        log.debug("Retrieved {} developer accounts", result.size());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/developers")
    public ResponseEntity<Map<String, Object>> createDeveloper(@Valid @RequestBody CreateDeveloperRequestDto request) {
        log.info("REST request to create developer account with email: [{}]", request.email());
        Map<String, Object> created = accountService.createDeveloper(request);
        log.info("Developer account created successfully for email: [{}]", request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/developers/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id) {
        log.info("REST request to regenerate setup/reset link for developer ID: [{}]", id);
        accountService.regenerateSetupLink(id);
        log.info("Password setup link successfully dispatched for developer ID: [{}]", id);
        return ResponseEntity.ok(Map.of("message", "Password setup link sent."));
    }

    @PatchMapping("/developers/{id}/status")
    public ResponseEntity<Map<String, String>> setDeveloperEnabled(
            @PathVariable Long id,
            @Valid @RequestBody EnabledRequestDto request
    ) {
        log.info("REST request to update status for developer ID: [{}] to enabled=[{}]", id, request.enabled());
        accountService.setDeveloperEnabled(id, request.enabled());
        log.info("Developer ID: [{}] status successfully changed to enabled=[{}]", id, request.enabled());
        return ResponseEntity.ok(Map.of("message", "Developer status updated."));
    }

    @DeleteMapping("/developers/{id}")
    public ResponseEntity<Void> deleteDeveloper(@PathVariable Long id) {
        log.warn("REST request to delete developer account ID: [{}]", id);
        accountService.deleteDeveloper(id);
        log.info("Developer account ID: [{}] successfully deleted", id);
        return ResponseEntity.noContent().build();
    }

    /*
     * HELPER
     * */

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
}
