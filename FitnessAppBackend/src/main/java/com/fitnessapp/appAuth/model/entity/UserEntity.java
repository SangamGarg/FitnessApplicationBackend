package com.fitnessapp.appAuth.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fitnessapp.appAuth.model.enums.MobileAccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @Column(name = "user_id")
    private String id; // Firebase UID

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20,
            columnDefinition = "varchar(20) default 'ACTIVE'")
    private MobileAccountStatus accountStatus = MobileAccountStatus.ACTIVE;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private UserDetailsEntity userDetailsEntity;

    /** All app installations currently associated with this account. */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<UserDeviceEntity> devices = new ArrayList<>();

    public void addDevice(UserDeviceEntity device) {
        devices.add(device);
        device.setUser(this);
    }

    public void removeDevice(UserDeviceEntity device) {
        devices.remove(device);
        device.setUser(null);
    }

    @PrePersist
    protected void onCreate() {
        if (accountStatus == null) {
            accountStatus = MobileAccountStatus.ACTIVE;
        }
    }
}
