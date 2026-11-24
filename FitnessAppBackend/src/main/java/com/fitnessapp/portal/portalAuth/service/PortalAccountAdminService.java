package com.fitnessapp.portal.portalAuth.service;

import com.fitnessapp.portal.audit.service.PortalAuditService;
import com.fitnessapp.portal.portalAuth.model.dto.request.CreateDeveloperRequestDto;
import com.fitnessapp.portal.portalAuth.model.dto.request.UpdatePortalAccountRequestDto;
import com.fitnessapp.portal.portalAuth.model.dto.response.PortalAccountResponseDto;
import com.fitnessapp.portal.portalAuth.model.entity.PortalAccountEntity;
import com.fitnessapp.portal.portalAuth.model.enums.UserRole;
import com.fitnessapp.portal.portalAuth.repository.PortalAccountRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortalAccountAdminService {

    private final PortalAccountRepository portalAccountRepository;
    private final PortalAuditService audit;
    private final JavaMailSender mailSender;
    private static final int SETUP_TOKEN_EXPIRY_HOURS = 24;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$";
    private final SecureRandom random = new SecureRandom();

    @Value("${fitness.portal.base-url:http://localhost:5173}")
    private String portalBaseUrl;


    @Transactional(readOnly = true)
    public Page<PortalAccountResponseDto> listPortalAccounts(
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

        return portalAccountRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PortalAccountResponseDto getPortalAccount(Long id) {
        return toResponse(findPortalAccount(id));
    }

    @Transactional
    public PortalAccountResponseDto updateDeveloperDetails(Long id, UpdatePortalAccountRequestDto request) {
        PortalAccountEntity account = findPortalAccount(id);
        if (account.getRole() != UserRole.DEVELOPER) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Developer account not found");
        }
        account.setFullName(request.name().trim());
        account.setMobileNumber(normalizeOptional(request.mobileNumber()));
        account.setGenderPortal(request.genderPortal());
        account.setAddress(normalizeOptional(request.address()));
        account.setUpdatedAt(Instant.now());

        audit.record("PORTAL_ACCOUNT_UPDATED", "PORTAL_ACCOUNT", id.toString(), "Developer profile updated");
        return toResponse(portalAccountRepository.save(account));
    }


    /**
     * Creates a developer account and emails a one-time password setup link.
     */
    @Transactional
    public Map<String, Object> createDeveloper(CreateDeveloperRequestDto request) {
        if (request == null) {
            log.warn("Attempted developer creation with null payload");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Developer details are required");
        }

        String name = normalizeRequired(request.name(), "Name");
        String email = normalizeEmail(request.email());

        if (portalAccountRepository.existsByEmailIgnoreCase(email)) {
            log.warn("Conflict creating developer: email [{}] already exists", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        String token = PortalAuthService.newSetupToken(random);
        Instant now = Instant.now();

        PortalAccountEntity account = new PortalAccountEntity();
        account.setFullName(name);
        account.setEmail(email);
        account.setMobileNumber(normalizeOptional(request.mobileNumber()));
        account.setGenderPortal(request.genderPortal());
        account.setAddress(normalizeOptional(request.address()));
        account.setRole(UserRole.DEVELOPER);
        account.setEnabled(true);
        account.setPasswordHash(null);
        account.setSetupTokenHash(PortalAuthService.sha256(token));
        account.setSetupTokenExpiresAt(now.plus(SETUP_TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS));
        account.setCreatedAt(now);
        account.setUpdatedAt(now);

        PortalAccountEntity saved = portalAccountRepository.saveAndFlush(account);

        sendSetupLink(
                saved.getFullName(),
                saved.getEmail(),
                token,
                "Set up your Fitness App developer account",
                "Your developer account has been created. Use the link below to set your password."
        );

        audit.record("DEVELOPER_ACCOUNT_CREATED", "PORTAL_ACCOUNT", saved.getId().toString(), "Developer invited");
        log.info("Developer account created successfully: accountId=[{}], email=[{}]", saved.getId(), email);
        return toDeveloperResponse(saved);
    }

    /**
     * Returns all developer accounts ordered by creation date descending.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listDevelopers() {
        log.debug("Fetching all developer accounts");
        return portalAccountRepository.findAllByRoleIdOrderByCreatedAtDesc(UserRole.DEVELOPER.getId()).stream()
                .map(this::toDeveloperResponse)
                .toList();
    }

    /**
     * Resends a password setup link and invalidates the previous token.
     */
    @Transactional
    public Map<String, Object> regenerateSetupLink(Long developerId) {
        PortalAccountEntity account = findDeveloperEntity(developerId);

        if (!account.isEnabled()) {
            log.warn("Cannot regenerate setup link for disabled developer account: [{}]", developerId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot generate a setup link for a disabled developer");
        }

        String token = PortalAuthService.newSetupToken(random);
        Instant now = Instant.now();

        account.setPasswordHash(null);
        account.setSetupTokenHash(PortalAuthService.sha256(token));
        account.setSetupTokenExpiresAt(now.plus(SETUP_TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS));
        account.setUpdatedAt(now);

        PortalAccountEntity saved = portalAccountRepository.saveAndFlush(account);

        sendSetupLink(
                saved.getFullName(),
                saved.getEmail(),
                token,
                "Your Fitness App password setup link",
                "An administrator has generated a new password setup link for your account."
        );

        audit.record("DEVELOPER_SETUP_LINK_REISSUED", "PORTAL_ACCOUNT", saved.getId().toString(), "Password setup link reissued");
        log.info("Developer setup link regenerated successfully: accountId=[{}]", saved.getId());
        return toDeveloperResponse(saved);
    }

    /**
     * Enables or disables a developer account.
     */
    @Transactional
    public Map<String, Object> setDeveloperEnabled(Long developerId, boolean enabled) {
        PortalAccountEntity account = findDeveloperEntity(developerId);

        account.setEnabled(enabled);
        account.setUpdatedAt(Instant.now());

        PortalAccountEntity saved = portalAccountRepository.saveAndFlush(account);
        audit.record(
                enabled ? "DEVELOPER_ACCOUNT_ACTIVATED" : "DEVELOPER_ACCOUNT_SUSPENDED",
                "PORTAL_ACCOUNT",
                saved.getId().toString(),
                "Developer account enabled=" + enabled
        );
        log.info("Developer account status changed: accountId=[{}], enabled=[{}]", saved.getId(), enabled);

        return toDeveloperResponse(saved);
    }

    /**
     * Deletes a developer account.
     */
    @Transactional
    public void deleteDeveloper(Long developerId) {
        PortalAccountEntity account = findDeveloperEntity(developerId);
        audit.record("DEVELOPER_ACCOUNT_DELETED", "PORTAL_ACCOUNT", developerId.toString(), "Developer account deleted");
        portalAccountRepository.delete(account);
        log.info("Developer account deleted successfully: accountId=[{}]", developerId);
    }


    private PortalAccountEntity findDeveloperEntity(Long developerId) {
        if (developerId == null) {
            log.warn("Developer search rejected: null ID supplied");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Developer ID is required");
        }

        PortalAccountEntity account = portalAccountRepository.findById(developerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Developer account not found"));

        if (account.getRole() != UserRole.DEVELOPER) {
            log.warn("Account ID [{}] requested as developer but has role [{}]", developerId, account.getRole());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Developer account not found");
        }

        return account;
    }

    private Map<String, Object> toDeveloperResponse(PortalAccountEntity account) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("id", account.getId());
        response.put("name", account.getFullName());
        response.put("email", account.getEmail());
        response.put("mobileNumber", account.getMobileNumber());
        response.put("genderPortal", account.getGenderPortal());
        response.put("address", account.getAddress());
        response.put("role", account.getRole().name());
        response.put("roleId", account.getRole().getId());

        String status;
        if (!account.isEnabled()) {
            status = "DISABLED";
        } else if (account.getPasswordHash() == null || account.getPasswordHash().isBlank()) {
            status = "INVITED";
        } else {
            status = "ACTIVE";
        }

        response.put("status", status);
        response.put("createdAt", account.getCreatedAt());
        response.put("updatedAt", account.getUpdatedAt());

        return response;
    }

    private void sendSetupLink(
            String name,
            String email,
            String token,
            String subject,
            String intro
    ) {
        String baseUrl = portalBaseUrl.replaceAll("/+$", "");
        String link = baseUrl + "/setup?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        String safeName = escapeHtml(name);
        String safeIntro = escapeHtml(intro);
        String safeLink = escapeHtml(link);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(email);
            helper.setSubject(subject);

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <body style="font-family:Arial,sans-serif;max-width:620px;margin:auto;padding:24px">
                        <h2>Hello %s</h2>
                        <p>%s</p>
                        <p>
                            <a href="%s"
                               style="display:inline-block;padding:12px 18px;background:#2563eb;color:#ffffff;text-decoration:none;border-radius:6px">
                               Set portal password
                            </a>
                        </p>
                        <p>This link expires in 24 hours and can be used only once.</p>
                        <p>If you were not expecting this email, contact your administrator.</p>
                        <p>Fitness App Team</p>
                    </body>
                    </html>
                    """.formatted(safeName, safeIntro, safeLink);

            helper.setText(html, true);
            mailSender.send(message);

            log.info("Developer setup email dispatched successfully to: [{}]", email);

        } catch (Exception ex) {
            log.error("Developer setup email delivery failed for user: [{}]", email, ex);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Email delivery failed; the account operation was rolled back",
                    ex
            );
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private String normalizeEmail(String email) {
        String normalized = normalizeRequired(email, "Email").toLowerCase(Locale.ROOT);

        if (!normalized.matches(EMAIL_REGEX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid email address is required");
        }

        return normalized;
    }

    private String normalizeOptional(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Finds one developer and returns its safe response representation.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> findDeveloper(Long developerId) {
        return toDeveloperResponse(findDeveloperEntity(developerId));
    }

    private PortalAccountEntity findPortalAccount(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account ID must be positive");
        }
        return portalAccountRepository.findById(id)
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
                account.getGenderPortal(),
                account.getAddress(),
                account.getRole(),
                account.isEnabled(),
                status,
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
