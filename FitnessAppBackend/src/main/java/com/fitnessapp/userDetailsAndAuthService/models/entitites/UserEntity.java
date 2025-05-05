package com.fitnessapp.userDetailsAndAuthService.models.entitites;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    // Marks this field as the primary key of the UserEntity table.
// The actual database column name will be "user_id".
    @Id
    @Column(name = "user_id")
    private String id; // Firebase UID (unique identifier from Firebase)

    // Declares a column "email" which cannot be null and must be unique across all users.
// Used to store the user's email address.
    @Column(nullable = false, unique = true)
    private String email;

    // Establishes a one-to-one bidirectional relationship with UserDetailsEntity.
// `mappedBy = "user"` tells JPA that the "user" field in UserDetailsEntity owns the relationship.
// `cascade = CascadeType.ALL` ensures any changes (persist, merge, remove, etc.) in UserEntity
// will automatically be applied to UserDetailsEntity.
// `orphanRemoval = true` means if you remove the association (set userDetailsEntity to null),
// the corresponding UserDetailsEntity record will be deleted from the database.
// `fetch = FetchType.LAZY` means UserDetailsEntity will not be loaded from DB until accessed.
// `@JsonManagedReference` is used to manage JSON serialization (avoids infinite recursion).
    @OneToOne(mappedBy = "users", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private UserDetailsEntity userDetailsEntity;

}