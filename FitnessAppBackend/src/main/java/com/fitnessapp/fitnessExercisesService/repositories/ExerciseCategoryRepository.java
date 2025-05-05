package com.fitnessapp.fitnessExercisesService.repositories;

import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseCategoryEntity;
import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseCategoryRepository extends JpaRepository<ExerciseCategoryEntity, Long> {
    ExerciseCategoryEntity findByName(String name);
    List<ExerciseCategoryEntity> findByNameStartingWithIgnoreCase(String name);

}
