package com.fitnessapp.userDetailsAndAuthService.models.entitites;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_detail_id")
    private Long id;

    //    @Column(nullable = false, unique = true)
    //    private List<String> firebaseCloudMessagingToken = new ArrayList<>();

    // Optional: Store tokens differently; a List<String> can't be mapped directly.
    // Consider using a separate entity or @ElementCollection
//    @ElementCollection
//    @CollectionTable(name = "fcm_tokens", joinColumns = @JoinColumn(name = "user_details_id"))
//    @Column(name = "token")
//    private List<String> firebaseCloudMessagingToken = new ArrayList<>();

    private String name;
    private String gender;
    private Integer age;
    private String profileImageUrl;
    private float height;
    private float weight;
    private String activityLevel;
    private String goal;
    private float hip;
    private float neck;
    private float waist;

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
    private UserEntity users;

}
