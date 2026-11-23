package com.fitnessapp.exercise.repo;

import com.fitnessapp.exercise.model.entity.exerciseEntity.ExerciseLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciseLevelRepository extends JpaRepository<ExerciseLevel, Long> {
    Optional<ExerciseLevel> findByLevelKeyIgnoreCase(String levelKey);
}
