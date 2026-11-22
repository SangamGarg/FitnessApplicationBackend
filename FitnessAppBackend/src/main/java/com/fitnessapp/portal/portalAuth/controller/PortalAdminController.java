package com.fitnessapp.portal.portalAuth.controller;

import com.fitnessapp.portal.portalAuth.model.dto.request.CreateDeveloperRequestDto;
import com.fitnessapp.portal.portalAuth.model.dto.request.EnabledRequestDto;
import com.fitnessapp.portal.portalAuth.service.DeveloperAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_ADMIN_DEV_ENDPOINT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_ADMIN_DEV_ENDPOINT)
public class PortalAdminController {

    private final DeveloperAdminService developers;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        log.debug("REST request to list all developer accounts");
        List<Map<String, Object>> result = developers.listDevelopers();
        log.debug("Retrieved {} developer accounts", result.size());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateDeveloperRequestDto request) {
        log.info("REST request to create developer account with email: [{}]", request.email());
        Map<String, Object> created = developers.createDeveloper(request);
        log.info("Developer account created successfully for email: [{}]", request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/reset-password") public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id) {
        log.info("REST request to regenerate setup/reset link for developer ID: [{}]", id);
        developers.regenerateSetupLink(id);
        log.info("Password setup link successfully dispatched for developer ID: [{}]", id);
        return ResponseEntity.ok(Map.of("message", "Password setup link sent."));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> setStatus(
            @PathVariable Long id,
            @Valid @RequestBody EnabledRequestDto request
    ) {
        log.info("REST request to update status for developer ID: [{}] to enabled=[{}]", id, request.enabled());
        developers.setEnabled(id, request.enabled());
        log.info("Developer ID: [{}] status successfully changed to enabled=[{}]", id, request.enabled());
        return ResponseEntity.ok(Map.of("message", "Developer status updated."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.warn("REST request to delete developer account ID: [{}]", id);
        developers.deleteDeveloper(id);
        log.info("Developer account ID: [{}] successfully deleted", id);
        return ResponseEntity.noContent().build();
    }
}