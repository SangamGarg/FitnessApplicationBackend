package com.fitnessapp.exercise.app.repo;

import com.fitnessapp.exercise.app.model.entity.ExerciseCategoryEntity;
import com.fitnessapp.exercise.app.model.entity.ExerciseLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseLevelRepository extends JpaRepository<ExerciseLevelEntity, Long> {
    List<ExerciseLevelEntity> findByCategoryId(Long categoryId);

    ExerciseLevelEntity findByLevelIgnoreCaseAndCategory(String level, ExerciseCategoryEntity category);
}
