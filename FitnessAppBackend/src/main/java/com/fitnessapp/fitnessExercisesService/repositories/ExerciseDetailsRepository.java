package com.fitnessapp.fitnessExercisesService.repositories;

import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseDetailsRepository extends JpaRepository<ExerciseDetailEntity, Long> {
    List<ExerciseDetailEntity> findBySubCategoryId(Long subCategoryId);
    List<ExerciseDetailEntity> findBySubCategoryIdAndNameStartingWithIgnoreCase(Long subCategoryId, String namePrefix);

}
