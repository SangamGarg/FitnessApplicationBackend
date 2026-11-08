package com.fitnessapp.exercise.app.repo;

import com.fitnessapp.exercise.app.model.entity.entityNew.UserWorkout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface WorkoutAnalyticsRepository extends JpaRepository<UserWorkout, Long> {

    @Query(value = """
        SELECT
            w.id AS workoutId,
            w.completed_at AS completedAt,
            w.duration_seconds AS workoutDurationSeconds,
            wel.id AS exerciseLogId,
            ws.id AS setId,
            e.id AS exerciseId,
            e.exercise_key AS exerciseKey,
            e.name AS exerciseName,
            mg.id AS muscleGroupId,
            mg.muscle_key AS muscleGroupKey,
            mg.name AS muscleGroupName,
            em.role AS muscleRole,
            ws.set_number AS setNumber,
            ws.reps_completed AS repsCompleted,
            ws.weight_kg AS weightKg,
            ws.duration_seconds AS setDurationSeconds
        FROM user_workouts w
        JOIN workout_exercise_logs wel ON wel.workout_id = w.id
        JOIN exercises e ON e.id = wel.exercise_id
        JOIN workout_set_logs ws ON ws.exercise_log_id = wel.id
        LEFT JOIN exercise_muscles em ON em.exercise_id = e.id
        LEFT JOIN muscle_groups mg ON mg.id = em.muscle_group_id
        WHERE w.user_id = :userId
          AND w.status = 'COMPLETED'
          AND w.completed_at >= :fromInstant
          AND w.completed_at < :toExclusive
          AND (:exerciseId IS NULL OR e.id = :exerciseId)
        ORDER BY w.completed_at ASC, w.id ASC, e.id ASC, ws.set_number ASC
        """, nativeQuery = true)
    List<WorkoutAnalyticsRow> findCompletedSetRows(
            @Param("userId") String userId,
            @Param("fromInstant") Instant fromInstant,
            @Param("toExclusive") Instant toExclusive,
            @Param("exerciseId") Long exerciseId
    );
}
