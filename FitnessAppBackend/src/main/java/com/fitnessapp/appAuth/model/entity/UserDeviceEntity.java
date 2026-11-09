package com.fitnessapp.appAuth.model.entity;

import com.fitnessapp.appAuth.model.enums.DevicePlatform;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "app_user_devices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_app_user_devices_device_id", columnNames = "device_id"),
                @UniqueConstraint(name = "uk_app_user_devices_fcm_token", columnNames = "fcm_token")
        },
        indexes = {
                @Index(name = "idx_app_user_devices_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Random UUID generated and persisted by the app for this installation. */
    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    /** FCM registration token. Never expose this value in API responses or logs. */
    @Column(name = "fcm_token", nullable = false, length = 4096)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private DevicePlatform platform;

    @Column(name = "device_name", length = 120)
    private String deviceName;

    @Column(name = "app_version", length = 40)
    private String appVersion;

    @Builder.Default
    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled = true;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    @Column(name = "token_updated_at")
    private Instant tokenUpdatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** Protects concurrent updates to the same device row. */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_app_user_devices_user"))
    @ToString.Exclude
    private UserEntity user;
}
