package com.fitnessapp.exercise.app.repo;

import com.fitnessapp.exercise.app.model.entity.entityNew.WorkoutExerciseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorkoutExerciseLogRepository extends JpaRepository<WorkoutExerciseLog, Long> {
    Optional<WorkoutExerciseLog> findByIdAndWorkout_Id(Long id, Long workoutId);
}
