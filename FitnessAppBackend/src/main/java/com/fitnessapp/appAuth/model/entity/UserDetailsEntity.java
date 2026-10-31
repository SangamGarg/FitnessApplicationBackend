package com.fitnessapp.appAuth.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fitnessapp.appAuth.model.enums.ActivityLevel;
import com.fitnessapp.appAuth.model.enums.FitnessGoal;
import com.fitnessapp.appAuth.model.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "app_user_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_details_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Gender gender;

    private Integer age;

    @Column(name = "profile_image_url", length = 2048)
    private String profileImageUrl;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", length = 40)
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private FitnessGoal goal;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_app_user_details_user"))
    @JsonBackReference
    @ToString.Exclude
    private UserEntity user;
}
