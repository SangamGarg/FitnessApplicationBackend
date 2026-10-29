package com.fitnessapp.appAuth.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "app_email_otp",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_app_email_otp_email_purpose",
                columnNames = {"email", "purpose"}
        ),
        indexes = @Index(name = "idx_app_email_otp_expiry", columnList = "expires_at")
)
@Getter
@Setter
@NoArgsConstructor
public class OtpEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 32)
    private String purpose;

    @Column(name = "otp_hash", length = 100)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_sent_at")
    private Instant lastSentAt;

    @Column(name = "window_started_at")
    private Instant windowStartedAt;

    @Column(name = "send_count", nullable = false)
    private int sendCount;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verification_token_hash", length = 100)
    private String verificationTokenHash;

    @Column(name = "verification_token_expires_at")
    private Instant verificationTokenExpiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
