package com.fitnessapp.nutrition.repository;

import com.fitnessapp.nutrition.model.entity.NutritionGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface NutritionGoalRepository extends JpaRepository<NutritionGoalEntity, UUID> {
    Optional<NutritionGoalEntity> findByUser_Id(String userId);
}
