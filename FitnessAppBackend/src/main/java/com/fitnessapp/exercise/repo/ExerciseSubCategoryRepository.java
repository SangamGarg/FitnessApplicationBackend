package com.fitnessapp.exercise.repo;

import com.fitnessapp.exercise.model.entity.exerciseEntity.ExerciseCategory;
import com.fitnessapp.exercise.model.entity.exerciseEntity.ExerciseSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciseSubCategoryRepository extends JpaRepository<ExerciseSubCategory, Long> {
    Optional<ExerciseSubCategory> findByCategoryAndSubCategoryKeyIgnoreCase(
            ExerciseCategory category, String subCategoryKey);
}
