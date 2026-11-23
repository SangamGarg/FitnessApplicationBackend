package com.fitnessapp.exercise.repo;

import com.fitnessapp.exercise.model.entity.workoutPlanEntity.WorkoutPlanExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutPlanExerciseRepository extends JpaRepository<WorkoutPlanExercise, Long> {
    boolean existsByExercise_Id(Long exerciseId);
}
