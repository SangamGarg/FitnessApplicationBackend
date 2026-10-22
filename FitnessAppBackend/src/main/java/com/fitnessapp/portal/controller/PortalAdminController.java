package com.fitnessapp.portal.controller;

import com.fitnessapp.portal.dto.CreateDeveloperRequestDto;
import com.fitnessapp.portal.dto.EnabledRequestDto;
import com.fitnessapp.portal.service.DeveloperAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.fitnessapp.portal.common.PortalUtils.PORTAL_ADMIN_DEVELOPER_BASE_REQUEST_URL;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_ADMIN_DEVELOPER_BASE_REQUEST_URL)
@Tag(name = "Admin - Developers", description = "Admin-only developer account lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class PortalAdminController {

    private final DeveloperAdminService developers;

    @GetMapping
    @Operation(summary = "List developer accounts")
    public ResponseEntity<List<Map<String, Object>>> list() {
        log.debug("REST request to list all developer accounts");
        List<Map<String, Object>> result = developers.listDevelopers();
        log.debug("Retrieved {} developer accounts", result.size());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Create developer and email password setup link")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateDeveloperRequestDto request) {
        log.info("REST request to create developer account with email: [{}]", request.email());
        Map<String, Object> created = developers.createDeveloper(request);
        log.info("Developer account created successfully for email: [{}]", request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Send a new one-time password setup link")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id) {
        log.info("REST request to regenerate setup/reset link for developer ID: [{}]", id);
        developers.regenerateSetupLink(id);
        log.info("Password setup link successfully dispatched for developer ID: [{}]", id);
        return ResponseEntity.ok(Map.of("message", "Password setup link sent."));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Enable or disable developer account")
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
    @Operation(summary = "Delete developer portal account")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.warn("REST request to delete developer account ID: [{}]", id);
        developers.deleteDeveloper(id);
        log.info("Developer account ID: [{}] successfully deleted", id);
        return ResponseEntity.noContent().build();
    }
}