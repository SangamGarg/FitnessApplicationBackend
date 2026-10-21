package com.fitnessapp.portal.tableEntity;

import com.fitnessapp.portal.common.enums.Gender;
import com.fitnessapp.portal.common.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString(exclude = {"passwordHash", "setupTokenHash"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "portal_accounts", indexes = {
        @Index(name = "uk_portal_accounts_email", columnList = "email", unique = true),
        @Index(name = "idx_portal_accounts_role_enabled", columnList = "role_id, enabled"),
        @Index(name = "idx_portal_accounts_setup_token", columnList = "setup_token_hash", unique = true)
})
public class PortalAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "mobile_number", length = 25)
    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private Gender gender;

    @Column(length = 500)
    private String address;

    /** Stable numeric role ID: 1=ADMIN, 2=DEVELOPER. */
    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "setup_token_hash", length = 64, unique = true)
    private String setupTokenHash;

    @Column(name = "setup_token_expires_at")
    private Instant setupTokenExpiresAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @Transient
    public UserRole getRole() {
        return this.roleId != null ? UserRole.fromId(this.roleId) : null;
    }

    public void setRole(UserRole role) {
        this.roleId = (role != null) ? role.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortalAccountEntity that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}