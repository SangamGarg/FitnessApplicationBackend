package com.fitnessapp.nutrition.model.entity;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import com.fitnessapp.nutrition.model.enums.DietFitnessGoal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "nutrition_user_preferences",
       uniqueConstraints = @UniqueConstraint(name = "uk_nutrition_pref_user", columnNames = "user_id"))
@Getter @Setter @NoArgsConstructor
public class UserDietPreferenceEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
        foreignKey = @ForeignKey(name = "fk_nutrition_pref_user"))
    private UserEntity user;
    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_preference", nullable = false, length = 20)
    private DietaryPreference dietaryPreference = DietaryPreference.ALL;
    @Enumerated(EnumType.STRING)
    @Column(name = "fitness_goal", nullable = false, length = 30)
    private DietFitnessGoal fitnessGoal = DietFitnessGoal.MAINTENANCE;
    @Column(name = "allergies", length = 1000)
    private String allergies;
    @Column(name = "excluded_ingredients", length = 1500)
    private String excludedIngredients;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
