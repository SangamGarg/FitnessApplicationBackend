package com.fitnessapp.portal.service;

import com.fitnessapp.portal.audit.PortalAuditService;
import com.fitnessapp.portal.common.enums.UserRole;
import com.fitnessapp.portal.dto.PortalAccountResponseDto;
import com.fitnessapp.portal.dto.UpdatePortalAccountRequestDto;
import com.fitnessapp.portal.repository.PortalAccountRepository;
import com.fitnessapp.portal.tableEntity.PortalAccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PortalAccountAdminService {

    private final PortalAccountRepository accounts;
    private final PortalAuditService audit;

    @Transactional(readOnly = true)
    public Page<PortalAccountResponseDto> list(
            String query,
            UserRole role,
            Boolean enabled,
            Pageable pageable
    ) {
        String normalizedQuery = query == null || query.isBlank()
                ? null
                : query.trim().toLowerCase(Locale.ROOT);

        Specification<PortalAccountEntity> specification = (root, criteria, builder) -> {
            var predicates = builder.conjunction();
            if (role != null) {
                predicates = builder.and(predicates, builder.equal(root.get("roleId"), role.getId()));
            }
            if (enabled != null) {
                predicates = builder.and(predicates, builder.equal(root.get("enabled"), enabled));
            }
            if (normalizedQuery != null) {
                String pattern = "%" + normalizedQuery + "%";
                predicates = builder.and(predicates, builder.or(
                        builder.like(builder.lower(root.get("email")), pattern),
                        builder.like(builder.lower(root.get("fullName")), pattern),
                        builder.like(builder.lower(root.get("mobileNumber")), pattern)
                ));
            }
            return predicates;
        };

        return accounts.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PortalAccountResponseDto get(Long id) {
        return toResponse(findAccount(id));
    }

    @Transactional
    public PortalAccountResponseDto updateDeveloper(Long id, UpdatePortalAccountRequestDto request) {
        PortalAccountEntity account = findDeveloper(id);
        account.setFullName(request.name().trim());
        account.setMobileNumber(normalizeOptional(request.mobileNumber()));
        account.setGender(request.gender());
        account.setAddress(normalizeOptional(request.address()));
        account.setUpdatedAt(Instant.now());

        audit.record("PORTAL_ACCOUNT_UPDATED", "PORTAL_ACCOUNT", id.toString(), "Developer profile updated");
        return toResponse(accounts.save(account));
    }

    @Transactional
    public PortalAccountResponseDto setDeveloperEnabled(Long id, boolean enabled) {
        PortalAccountEntity account = findDeveloper(id);
        account.setEnabled(enabled);
        account.setUpdatedAt(Instant.now());
        audit.record(
                enabled ? "PORTAL_ACCOUNT_ACTIVATED" : "PORTAL_ACCOUNT_SUSPENDED",
                "PORTAL_ACCOUNT",
                id.toString(),
                "Developer account enabled=" + enabled
        );
        return toResponse(accounts.save(account));
    }

    private PortalAccountEntity findDeveloper(Long id) {
        PortalAccountEntity account = findAccount(id);
        if (account.getRole() != UserRole.DEVELOPER) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Developer account not found");
        }
        return account;
    }

    private PortalAccountEntity findAccount(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account ID must be positive");
        }
        return accounts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portal account not found"));
    }

    private PortalAccountResponseDto toResponse(PortalAccountEntity account) {
        String status = !account.isEnabled()
                ? "DISABLED"
                : account.getPasswordHash() == null || account.getPasswordHash().isBlank()
                ? "INVITED"
                : "ACTIVE";
        return new PortalAccountResponseDto(
                account.getId(),
                account.getFullName(),
                account.getEmail(),
                account.getMobileNumber(),
                account.getGender(),
                account.getAddress(),
                account.getRole(),
                account.isEnabled(),
                status,
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
