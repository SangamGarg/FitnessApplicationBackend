package com.fitnessapp.fitnessCalculatorsService.repositories;

import com.fitnessapp.fitnessCalculatorsService.models.entities.CaloriesInFoodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaloriesInFoodRepository extends JpaRepository<CaloriesInFoodEntity, Long> {
    Optional<CaloriesInFoodEntity> findByNameIgnoreCase(String name);
    List<CaloriesInFoodEntity> findByNameStartingWithIgnoreCase(String name);
}