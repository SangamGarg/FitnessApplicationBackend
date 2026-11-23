package com.fitnessapp.exercise.repo;

import com.fitnessapp.exercise.model.entity.exerciseEntity.ExerciseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciseCategoryRepository extends JpaRepository<ExerciseCategory, Long> {
    Optional<ExerciseCategory> findByCategoryKeyIgnoreCase(String categoryKey);
}
