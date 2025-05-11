package com.fitnessapp.fitnessExercisesService.repositories;

import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseDetailEntity;
import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseSubCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseSubCategoryRepository extends JpaRepository<ExerciseSubCategoryEntity, Long> {
    List<ExerciseSubCategoryEntity> findByLevelId(Long levelId);

    List<ExerciseSubCategoryEntity> findByLevelIdAndNameStartingWithIgnoreCase(Long levelId, String namePrefix);
}

