package com.fitnessapp.nutrition.repository;

import com.fitnessapp.nutrition.model.entity.NutritionGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NutritionGoalRepository extends JpaRepository<NutritionGoalEntity, UUID> {
    Optional<NutritionGoalEntity> findByUser_Id(String userId);
}
