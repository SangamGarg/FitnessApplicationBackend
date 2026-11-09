package com.fitnessapp.exercise.app.repo;

import com.fitnessapp.exercise.app.model.entity.ExerciseDetailEntity;
import com.fitnessapp.exercise.app.model.entity.ExerciseSubCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseDetailsRepository extends JpaRepository<ExerciseDetailEntity, Long> {
    List<ExerciseDetailEntity> findBySubCategoryId(Long subCategoryId);
    List<ExerciseDetailEntity> findBySubCategoryIdAndNameStartingWithIgnoreCase(Long subCategoryId, String namePrefix);

    ExerciseDetailEntity findByNameIgnoreCaseAndSubCategory(String name, ExerciseSubCategoryEntity subCategory);
}
