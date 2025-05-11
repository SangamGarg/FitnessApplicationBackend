package com.fitnessapp.fitnessCalculatorsService.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "burned_calories_activity")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BurnedCaloriesActivityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
    private Double metValue;
    private String imageUrl;
}
