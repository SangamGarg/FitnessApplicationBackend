package com.fitnessapp.fitnessExercises.repositories;

import com.fitnessapp.fitnessExercises.models.entities.ExerciseDetailEntity;
import com.fitnessapp.fitnessExercises.models.entities.ExerciseSubCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseDetailsRepository extends JpaRepository<ExerciseDetailEntity, Long> {
    List<ExerciseDetailEntity> findBySubCategoryId(Long subCategoryId);
    List<ExerciseDetailEntity> findBySubCategoryIdAndNameStartingWithIgnoreCase(Long subCategoryId, String namePrefix);

    ExerciseDetailEntity findByNameIgnoreCaseAndSubCategory(String name, ExerciseSubCategoryEntity subCategory);
}
