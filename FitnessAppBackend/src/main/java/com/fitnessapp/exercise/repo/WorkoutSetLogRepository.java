package com.fitnessapp.exercise.repo;

import com.fitnessapp.exercise.model.entity.WorkoutSetLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorkoutSetLogRepository extends JpaRepository<WorkoutSetLog, Long> {
    Optional<WorkoutSetLog> findByExerciseLog_IdAndSetNumber(Long exerciseLogId, short setNumber);
}
