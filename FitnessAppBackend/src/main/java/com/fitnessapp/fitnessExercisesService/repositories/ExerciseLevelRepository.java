package com.fitnessapp.fitnessExercisesService.repositories;

import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseCategoryEntity;
import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseLevelRepository extends JpaRepository<ExerciseLevelEntity, Long> {
    List<ExerciseLevelEntity> findByCategoryId(Long categoryId);

    ExerciseLevelEntity findByLevelIgnoreCaseAndCategory(String level, ExerciseCategoryEntity category);
}
