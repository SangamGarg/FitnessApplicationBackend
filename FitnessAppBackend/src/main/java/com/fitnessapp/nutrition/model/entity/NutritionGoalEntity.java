package com.fitnessapp.nutrition.model.entity;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "nutrition_user_goals",
       uniqueConstraints = @UniqueConstraint(name = "uk_nutrition_goal_user", columnNames = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class NutritionGoalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_nutrition_goal_user"))
    private UserEntity user;

    @Column(name = "daily_calories", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyCalories;

    @Column(name = "daily_protein", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyProtein;

    @Column(name = "daily_carbohydrates", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyCarbohydrates;

    @Column(name = "daily_fat", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyFat;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
