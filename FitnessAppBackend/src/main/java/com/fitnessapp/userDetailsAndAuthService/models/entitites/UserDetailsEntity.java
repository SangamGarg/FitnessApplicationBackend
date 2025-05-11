package com.fitnessapp.userDetailsAndAuthService.models.entitites;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_details_id")
    private Long id;


    //If your entity has a list, set, or map of simple or embeddable objects that don’t need to be standalone entities (with their own primary key), you use @ElementCollection.
    @ElementCollection(fetch = FetchType.LAZY)
    @Column(nullable = false, unique = true)
    @CollectionTable(name = "fcm_tokens", joinColumns = @JoinColumn(name = "user_details_id"))
    private List<String> firebaseCloudMessagingToken = new ArrayList<>();

    @Embedded
    private UserBodyDetails userBodyDetails;

    @Column(unique = true)
    private String email;
    private String name;
    private String gender;
    private Integer age;
    private String profileImageUrl;
    private Float height;
    private Float weight;
    private String activityLevel;
    private String goal;
    private Float hip;
    private Float neck;
    private Float waist;

    // Establishes a one-to-one relationship with the UserEntity class.
// `optional = false` means this association is mandatory —
// a UserDetailsEntity cannot exist without being linked to a UserEntity.
    @OneToOne(optional = false)

// Specifies the foreign key column in the "users_details" table that references the primary key of the "users" table.
// The column will be named "user_id" and cannot be null, enforcing the one-to-one link at the database level.
    @JoinColumn(name = "user_id", nullable = false)

// Marks this side of the relationship as the inverse side for JSON serialization.
// Prevents infinite recursion when converting entities to JSON by skipping this field during serialization.
    @JsonBackReference
    private UserEntity user;
}
