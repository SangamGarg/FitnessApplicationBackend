package com.fitnessapp.exercise.model.dto.response;

import com.fitnessapp.exercise.model.enums.ExerciseDifficulty;
import com.fitnessapp.exercise.model.enums.ExerciseType;
import com.fitnessapp.exercise.model.enums.WorkoutStatus;

import java.time.Instant;
import java.util.List;

/**
 * Single payload the client loads when it navigates to the Exercise screen.
 * Personal sections (activeWorkout, stats, recentWorkouts) are null / empty
 * when the request is not authenticated.
 */
public record ExerciseHomeResponseDto(
        String title,
        String subTitle,
        String description,
        ActiveWorkout activeWorkout,
        Stats stats,
        List<RecentWorkout> recentWorkouts,
        List<PlanSummary> featuredPlans,
        List<MuscleGroupSummary> muscleGroups,
        List<ExerciseSummary> newExercises,
        Filters filters
) {

    /** In-progress or paused session, so the client can show a "Resume workout" card. */
    public record ActiveWorkout(
            Long id,
            WorkoutStatus status,
            Instant startedAt,
            Long workoutPlanId,
            int exerciseCount
    ) {
    }

    public record Stats(long totalCompletedWorkouts, long completedLast7Days) {
    }

    public record RecentWorkout(
            Long id,
            Long workoutPlanId,
            Instant startedAt,
            Instant completedAt,
            Integer durationSeconds,
            int exerciseCount
    ) {
    }

    public record PlanSummary(
            Long id,
            String planKey,
            String name,
            String goal,
            String difficulty,
            int durationWeeks,
            int daysPerWeek,
            Integer estimatedSessionMinutes
    ) {
    }

    /** Use muscleKey with GET /catalog/by-muscle?muscle={muscleKey}. */
    public record MuscleGroupSummary(String muscleKey, String name, long exerciseCount) {
    }

    public record ExerciseSummary(
            Long id,
            String exerciseKey,
            String name,
            ExerciseDifficulty difficulty,
            ExerciseType exerciseType,
            String equipment,
            String thumbnailUrl
    ) {
    }

    /** Values for the filter chips used with GET /catalog. */
    public record Filters(List<String> exerciseTypes, List<String> difficulties) {
    }
}