package com.fitnessapp.fitnessCalculators.repositories;

import com.fitnessapp.fitnessCalculators.models.entities.CaloriesInFoodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaloriesInFoodRepository extends JpaRepository<CaloriesInFoodEntity, Long> {
    Optional<CaloriesInFoodEntity> findByNameIgnoreCase(String name);
    List<CaloriesInFoodEntity> findByNameStartingWithIgnoreCase(String name);
}