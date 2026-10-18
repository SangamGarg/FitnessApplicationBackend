package com.fitnessapp.fitnessExercises.repositories;

import com.fitnessapp.fitnessExercises.models.entities.ExerciseLevelEntity;
import com.fitnessapp.fitnessExercises.models.entities.ExerciseSubCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseSubCategoryRepository extends JpaRepository<ExerciseSubCategoryEntity, Long> {
    List<ExerciseSubCategoryEntity> findByLevelId(Long levelId);

    List<ExerciseSubCategoryEntity> findByLevelIdAndNameStartingWithIgnoreCase(Long levelId, String namePrefix);

    ExerciseSubCategoryEntity findByNameIgnoreCaseAndLevel(String name, ExerciseLevelEntity level);
}

