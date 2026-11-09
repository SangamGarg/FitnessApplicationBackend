package com.fitnessapp.portal.common;

import com.fitnessapp.portal.tableEntity.PortalAccountEntity;
import com.fitnessapp.portal.repository.PortalAccountRepository;
import com.fitnessapp.portal.common.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Configuration
@Slf4j
public class PortalAdminBootstrap {
    @Bean
    ApplicationRunner seedPortalAdmin(PortalAccountRepository accounts, PasswordEncoder encoder,
                                      @Value("${fitness.portal.admin.email:}") String email,
                                      @Value("${fitness.portal.admin.password:}") String password,
                                      @Value("${fitness.portal.admin.name:Portal Admin}") String name) {
        return args -> {
            if (email == null || email.isBlank() || password == null || password.length() < 12) {
                log.warn("Initial portal admin not provisioned. Set FITNESS_PORTAL_ADMIN_EMAIL and FITNESS_PORTAL_ADMIN_PASSWORD (minimum 12 characters).");
                return;
            }
            String normalized = email.trim().toLowerCase(Locale.ROOT);
            var existing = accounts.findByEmailIgnoreCase(normalized);
            if (existing.isPresent()) {
                if (existing.get().getRole() != UserRole.ADMIN)
                    throw new IllegalStateException("Configured admin email already belongs to a non-admin account");
                log.info("Configured portal admin already exists (id={})", existing.get().getId());
                return;
            }
            PortalAccountEntity admin = new PortalAccountEntity();
            admin.setEmail(normalized);
            admin.setFullName(name.trim());
            admin.setRole(UserRole.ADMIN);
            admin.setPasswordHash(encoder.encode(password));
            admin.setEnabled(true);
            accounts.save(admin);
            log.info("Initial portal admin provisioned (id={}), (Email={})", admin.getId(),admin.getEmail());
        };
    }
}
