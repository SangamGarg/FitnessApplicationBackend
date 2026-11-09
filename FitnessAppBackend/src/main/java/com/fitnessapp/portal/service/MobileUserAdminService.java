package com.fitnessapp.portal.service;

import com.fitnessapp.portal.audit.PortalAuditService;
import com.fitnessapp.portal.dto.MobileUserAdminResponseDto;
import com.fitnessapp.appAuth.model.enums.MobileAccountStatus;
import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MobileUserAdminService {

    private final UserRepo users;
    private final PortalAuditService audit;

    @Transactional(readOnly = true)
    public Page<MobileUserAdminResponseDto> list(String query, MobileAccountStatus status, Pageable pageable) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        return users.searchForAdmin(normalizedQuery, status, pageable);
    }

    @Transactional(readOnly = true)
    public MobileUserAdminResponseDto get(String id) {
        return users.findAdminViewById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mobile user not found"));
    }

    @Transactional
    public MobileUserAdminResponseDto setStatus(String id, MobileAccountStatus status) {
        if (id == null || id.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
        }
        UserEntity user = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mobile user not found"));
        MobileAccountStatus previous = user.getAccountStatus();
        user.setAccountStatus(status);
        users.save(user);
        audit.record(
                "MOBILE_USER_STATUS_CHANGED",
                "MOBILE_USER",
                id,
                "Status changed from " + previous + " to " + status
        );
        return users.findAdminViewById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mobile user not found"));
    }
}
