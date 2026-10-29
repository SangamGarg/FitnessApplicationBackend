package com.fitnessapp.portal.controller;

import com.fitnessapp.portal.dto.MobileUserAdminResponseDto;
import com.fitnessapp.portal.dto.MobileUserStatusRequestDto;
import com.fitnessapp.portal.service.MobileUserAdminService;
import com.fitnessapp.appAuth.model.entity.MobileAccountStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_BASE_URL;

@RestController
@RequestMapping(PORTAL_BASE_URL + "/admin/users")
@Tag(name = "Admin - Mobile Users", description = "Search mobile users and manage account access")
@SecurityRequirement(name = "bearerAuth")
public class MobileUserAdminController {

    private final MobileUserAdminService service;

    public MobileUserAdminController(MobileUserAdminService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Search mobile users with pagination and status filtering")
    public ResponseEntity<Page<MobileUserAdminResponseDto>> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) MobileAccountStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(service.list(query, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a mobile user profile and account status")
    public ResponseEntity<MobileUserAdminResponseDto> get(@PathVariable String id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Suspend, reactivate, or deactivate a mobile user account")
    public ResponseEntity<MobileUserAdminResponseDto> setStatus(
            @PathVariable String id,
            @Valid @RequestBody MobileUserStatusRequestDto request
    ) {
        return ResponseEntity.ok(service.setStatus(id, request.status()));
    }
}
