package com.fitnessapp.fitnessCalculatorsService.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "calories_in_food")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaloriesInFoodEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double calories;
    private Double serving_size_g;
    private Double protein_g;
    private Double carbohydrates_total_g;
    private Double fiber_g;
    private Double fat_saturated_g;
    private Double fat_total_g;
    private Double cholesterol_mg;
    private Double sugar_g;
    private Double sodium_mg;
    private Double potassium_mg;
    private String imageUrl;
}
