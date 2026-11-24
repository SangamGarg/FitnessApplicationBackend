package com.fitnessapp.appAuth.controller;

import com.fitnessapp.appAuth.model.enums.MobileAccountStatus;
import com.fitnessapp.appAuth.service.MobileUserAdminService;
import com.fitnessapp.portal.portalAuth.model.dto.request.MobileUserStatusRequestDto;
import com.fitnessapp.portal.portalAuth.model.dto.response.MobileUserAdminResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_ADMIN_MOBILE_USERS_ENDPOINT;

@RestController
@RequestMapping(PORTAL_ADMIN_MOBILE_USERS_ENDPOINT)
@RequiredArgsConstructor
public class MobileUserAdminController {

    private final MobileUserAdminService service;

    @GetMapping
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
    public ResponseEntity<MobileUserAdminResponseDto> get(@PathVariable String id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MobileUserAdminResponseDto> setStatus(
            @PathVariable String id,
            @Valid @RequestBody MobileUserStatusRequestDto request
    ) {
        return ResponseEntity.ok(service.setStatus(id, request.status()));
    }
}
