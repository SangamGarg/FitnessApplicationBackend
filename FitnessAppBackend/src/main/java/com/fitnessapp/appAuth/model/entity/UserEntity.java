package com.fitnessapp.appAuth.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "app_users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    // Primary key. The database column is named "user_id".
    @Id
    @Column(name = "user_id")
    private String id; // Firebase UID (unique identifier from Firebase)

    // Cannot be null and must be unique across all users.
    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20, columnDefinition = "varchar(20) default 'ACTIVE'")
    private MobileAccountStatus accountStatus = MobileAccountStatus.ACTIVE;

    // One-to-one with UserDetailsEntity; "user" in UserDetailsEntity owns the relationship.
    // cascade ALL + orphanRemoval: deleting a user deletes their details (and FCM tokens).
    // The relationship is excluded from toString/equals/hashCode: both sides reference each other,
    // so including it would recurse forever (StackOverflowError).
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserDetailsEntity userDetailsEntity;

    @PrePersist
    protected void onCreate() {
        if (accountStatus == null) {
            accountStatus = MobileAccountStatus.ACTIVE;
        }
    }
}