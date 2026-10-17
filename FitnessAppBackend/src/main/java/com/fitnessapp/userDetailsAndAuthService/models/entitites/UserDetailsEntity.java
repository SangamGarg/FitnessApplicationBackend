package com.fitnessapp.userDetailsAndAuthService.models.entitites;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

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

    // A simple list of strings stored in its own table "fcm_tokens".
    // NOT unique: the same phone token may briefly exist on two accounts; the service removes it from
    // other users whenever it is registered (see UserDetailAndAuthRepository.removeTokensFromOtherUsers).
    @ElementCollection(fetch = FetchType.LAZY)
    @Column(nullable = false)
    @CollectionTable(name = "fcm_tokens", joinColumns = @JoinColumn(name = "user_details_id"))
    @Builder.Default
    private List<String> firebaseCloudMessagingToken = new ArrayList<>();

    @Embedded
    private UserBodyDetails userBodyDetails;

    @Column(unique = true)
    private String email;
    private String name;
    private String gender;
    private Integer age;
    private String profileImageUrl;
    private Float heightCm;
    private Float weightKg;
    private String activityLevel;
    private String goal;
    private Float hipCm;
    private Float neckCm;
    private Float waistCm;

    // Mandatory one-to-one link to UserEntity (foreign key column "user_id").
    // Excluded from toString/equals/hashCode to avoid infinite recursion with UserEntity.
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserEntity user;
}