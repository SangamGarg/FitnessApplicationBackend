package com.fitnessapp.exercise.repo;

import com.fitnessapp.exercise.model.entity.WorkoutExerciseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkoutExerciseLogRepository extends JpaRepository<WorkoutExerciseLog, Long> {
    Optional<WorkoutExerciseLog> findByIdAndWorkout_Id(Long id, Long workoutId);

    boolean existsByExercise_Id(Long exerciseId);
}
