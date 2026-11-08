package com.fitnessapp.nutrition.model.entity;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.nutrition.model.enums.MealType;
import com.fitnessapp.nutrition.model.enums.MealSyncStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "nutrition_meal_entries", indexes = {
    @Index(name = "idx_meal_user_date", columnList = "user_id, meal_date"),
    @Index(name = "idx_meal_user_date_type", columnList = "user_id, meal_date, meal_type")
}, uniqueConstraints = @UniqueConstraint(name = "uk_meal_user_client_id",
        columnNames = {"user_id", "client_meal_id"}))
@Getter @Setter @NoArgsConstructor
public class MealEntryEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_meal_entry_user"))
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", foreignKey = @ForeignKey(name = "fk_meal_entry_food"))
    private FoodItemEntity foodItem;
    @Column(name = "client_meal_id")
    private UUID clientMealId;
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;
    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;
    @Column(name = "food_name_snapshot", nullable = false, length = 160)
    private String foodName;
    @Column(name = "quantity_grams", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityGrams;
    @Column(name = "quantity_unit", length = 20)
    private String quantityUnit = "GRAMS";
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal calories;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal protein;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal carbohydrates;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fat;
    @Column(precision = 10, scale = 2)
    private BigDecimal fiber;
    @Column(precision = 10, scale = 2)
    private BigDecimal sugar;
    @Column(length = 500)
    private String notes;
    @Column(name = "photo_url", length = 2048)
    private String photoUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private MealSyncStatus syncStatus = MealSyncStatus.SYNCED;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
