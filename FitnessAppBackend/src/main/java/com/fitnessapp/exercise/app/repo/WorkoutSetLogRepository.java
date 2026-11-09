package com.fitnessapp.exercise.app.repo;

import com.fitnessapp.exercise.app.model.entity.entityNew.WorkoutSetLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorkoutSetLogRepository extends JpaRepository<WorkoutSetLog, Long> {
    Optional<WorkoutSetLog> findByExerciseLog_IdAndSetNumber(Long exerciseLogId, short setNumber);
}
