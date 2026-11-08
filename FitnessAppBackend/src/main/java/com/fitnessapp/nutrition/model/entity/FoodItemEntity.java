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
@Table(name = "nutrition_food_items", indexes = {
        @Index(name = "idx_food_name", columnList = "name"),
        @Index(name = "idx_food_owner_name", columnList = "owner_user_id, name")
})
@Getter
@Setter
@NoArgsConstructor
public class FoodItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id",
            foreignKey = @ForeignKey(name = "fk_food_item_owner"))
    private UserEntity owner;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 100)
    private String brand;

    @Column(name = "serving_label", nullable = false, length = 80)
    private String servingLabel = "100 g";

    @Column(name = "serving_size_grams", nullable = false, precision = 10, scale = 2)
    private BigDecimal servingSizeGrams = new BigDecimal("100.00");

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

    @Column(name = "is_verified", nullable = false)
    private boolean verified;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
