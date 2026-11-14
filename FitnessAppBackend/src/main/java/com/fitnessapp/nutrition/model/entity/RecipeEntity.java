package com.fitnessapp.nutrition.model.entity;

import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import com.fitnessapp.nutrition.model.enums.RecipeDifficulty;
import com.fitnessapp.appAuth.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "nutrition_recipes", indexes = {
        @Index(name = "idx_recipe_active", columnList = "is_active"),
        @Index(name = "idx_recipe_diet_type", columnList = "diet_type")
})
@Getter
@Setter
@NoArgsConstructor
public class RecipeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "prep_time_minutes", nullable = false)
    private Integer prepTimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecipeDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "diet_type", nullable = false, length = 20)
    private DietaryPreference dietType;

    @Column(name = "servings", nullable = false)
    private Integer servings = 1;

    @Column(name = "calories_per_serving", nullable = false, precision = 10, scale = 2)
    private BigDecimal calories;

    @Column(name = "protein_per_serving", nullable = false, precision = 10, scale = 2)
    private BigDecimal protein;

    @Column(name = "carbs_per_serving", nullable = false, precision = 10, scale = 2)
    private BigDecimal carbohydrates;

    @Column(name = "fat_per_serving", nullable = false, precision = 10, scale = 2)
    private BigDecimal fat;

    @Column(name = "fiber_per_serving", precision = 10, scale = 2)
    private BigDecimal fiber;

    @Column(name = "sugar_per_serving", precision = 10, scale = 2)
    private BigDecimal sugar;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "nutrition_recipe_ingredients",
            joinColumns = @JoinColumn(name = "recipe_id"))
    @OrderColumn(name = "ingredient_order")
    @Column(name = "ingredient", nullable = false, length = 240)
    private List<String> ingredients = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "nutrition_recipe_instructions",
            joinColumns = @JoinColumn(name = "recipe_id"))
    @OrderColumn(name = "step_order")
    @Column(name = "instruction", nullable = false, length = 1000)
    private List<String> instructions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", foreignKey = @ForeignKey(name = "fk_recipe_owner"))
    private UserEntity owner;

    @Column(name = "is_ai_generated", nullable = false)
    private boolean aiGenerated;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
