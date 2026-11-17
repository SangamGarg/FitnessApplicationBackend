package com.fitnessapp.exercise.service;

import com.fitnessapp.exercise.model.dto.request.ExerciseUpsertRequestDto;
import com.fitnessapp.exercise.model.dto.request.WorkoutPlanUpsertRequestDto;
import com.fitnessapp.exercise.model.dto.response.ExerciseHomeResponseDto;
import com.fitnessapp.exercise.model.dto.response.ExerciseResponseDto;
import com.fitnessapp.exercise.model.dto.response.WorkoutPlanResponseDto;
import com.fitnessapp.exercise.model.entity.UserWorkout;
import com.fitnessapp.exercise.model.entity.exerciseEntity.Exercise;
import com.fitnessapp.exercise.model.entity.exerciseEntity.ExerciseMedia;
import com.fitnessapp.exercise.model.entity.workoutPlanEntity.WorkoutPlan;
import com.fitnessapp.exercise.model.entity.workoutPlanEntity.WorkoutPlanDay;
import com.fitnessapp.exercise.model.entity.workoutPlanEntity.WorkoutPlanExercise;
import com.fitnessapp.exercise.model.enums.ExerciseDifficulty;
import com.fitnessapp.exercise.model.enums.ExerciseType;
import com.fitnessapp.exercise.model.enums.WorkoutStatus;
import com.fitnessapp.exercise.repo.ExerciseRepository;
import com.fitnessapp.exercise.repo.UserWorkoutRepository;
import com.fitnessapp.exercise.repo.WorkoutPlanRepository;
import com.fitnessapp.portal.audit.PortalAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseAppServiceImpl implements ExerciseAppService {

    private final ExerciseRepository exercises;
    private final WorkoutPlanRepository plans;
    private final UserWorkoutRepository workouts;
    private final ObjectMapper objectMapper;
    private final PortalAuditService audit;

    private static final int HOME_FEATURED_PLAN_LIMIT = 5;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ExerciseHomeResponseDto> getExerciseHome() {
        long totalExercises = exercises.countByPublishedTrue();

        List<ExerciseHomeResponseDto.MuscleGroupSummary> muscleGroups = exercises.countPublishedByMuscleGroup().stream()
                .map(row -> new ExerciseHomeResponseDto.MuscleGroupSummary(
                        row.getMuscleKey(),
                        row.getName(),
                        row.getExerciseCount() == null ? 0 : row.getExerciseCount()
                ))
                .toList();

        List<ExerciseHomeResponseDto.PlanSummary> featuredPlans = plans
                .search("", "", "", true, PageRequest.of(0, HOME_FEATURED_PLAN_LIMIT))
                .stream()
                .map(plan -> new ExerciseHomeResponseDto.PlanSummary(
                        plan.getId(),
                        plan.getPlanKey(),
                        plan.getName(),
                        plan.getGoal(),
                        plan.getDifficulty(),
                        plan.getDurationWeeks(),
                        plan.getDaysPerWeek(),
                        plan.getEstimatedSessionMinutes() == null ? null : plan.getEstimatedSessionMinutes().intValue()
                ))
                .toList();

        List<ExerciseHomeResponseDto.ExerciseSummary> newExercises = exercises
                .findTop6ByPublishedTrueOrderByCreatedAtDesc().stream()
                .map(this::toSummary)
                .toList();

        ExerciseHomeResponseDto.Filters filters = new ExerciseHomeResponseDto.Filters(
                Arrays.stream(ExerciseType.values()).map(Enum::name).toList(),
                Arrays.stream(ExerciseDifficulty.values()).map(Enum::name).toList()
        );

        // Personal sections: only filled for an authenticated user.
        ExerciseHomeResponseDto.ActiveWorkout activeWorkout = null;
        ExerciseHomeResponseDto.Stats stats = null;
        List<ExerciseHomeResponseDto.RecentWorkout> recentWorkouts = List.of();

        String userId = currentUserIdOrNull();
        if (userId != null) {
            activeWorkout = workouts
                    .findFirstByUserIdAndStatusInOrderByStartedAtDesc(
                            userId, List.of(WorkoutStatus.IN_PROGRESS, WorkoutStatus.PAUSED))
                    .map(w -> new ExerciseHomeResponseDto.ActiveWorkout(
                            w.getId(),
                            w.getStatus(),
                            w.getStartedAt(),
                            w.getWorkoutPlanId(),
                            w.getExerciseLogs().size()
                    ))
                    .orElse(null);

            stats = new ExerciseHomeResponseDto.Stats(
                    workouts.countByUserIdAndStatus(userId, WorkoutStatus.COMPLETED),
                    workouts.countByUserIdAndStatusAndCompletedAtGreaterThanEqual(
                            userId, WorkoutStatus.COMPLETED, Instant.now().minus(7, ChronoUnit.DAYS))
            );

            recentWorkouts = workouts.findTop3ByUserIdAndStatusOrderByStartedAtDesc(userId, WorkoutStatus.COMPLETED)
                    .stream()
                    .map(this::toRecentWorkout)
                    .toList();
        }

        String description = totalExercises == 0
                ? "Exercises are coming soon"
                : totalExercises + " exercises across " + muscleGroups.size() + " muscle groups";

        return ResponseEntity.ok(new ExerciseHomeResponseDto(
                "Exercises",
                "Find exercises, follow plans and track your workouts",
                description,
                activeWorkout,
                stats,
                recentWorkouts,
                featuredPlans,
                muscleGroups,
                newExercises,
                filters
        ));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseResponseDto> getPublishedExercisesList(
            String query,
            ExerciseDifficulty difficulty,
            ExerciseType type,
            Boolean published,
            Pageable pageable
    ) {
        String normalizedQuery = query == null || query.isBlank()
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);
        return exercises.search(normalizedQuery, difficulty, type, published, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseResponseDto getPublishedExercise(Long id, boolean publishedOnly) {
        Exercise exercise = findExercise(id);
        if (publishedOnly && !exercise.isPublished()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found");
        }
        return toResponse(exercise);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseResponseDto> searchExercisesByMuscleGroup(
            String muscle,
            String role,
            Boolean published,
            Pageable pageable
    ) {
        if (muscle == null || muscle.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Muscle group is required");
        }
        String normalizedRole = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (!normalizedRole.isEmpty() && !normalizedRole.equals("PRIMARY") && !normalizedRole.equals("SECONDARY")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be PRIMARY or SECONDARY");
        }
        return exercises.searchByMuscleGroup(
                muscle.trim().toLowerCase(Locale.ROOT),
                normalizedRole,
                published,
                pageable
        ).map(this::toResponse);
    }

    @Transactional
    @Override
    public ExerciseResponseDto createExercise(ExerciseUpsertRequestDto request) {
        if (exercises.existsByExerciseKeyIgnoreCase(request.exerciseKey().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise key already exists");
        }
        Exercise exercise = new Exercise();
        applyExercise(exercise, request);
        Exercise saved = exercises.save(exercise);
        audit.record("EXERCISE_CREATED", "EXERCISE", saved.getId().toString(), "Exercise created as draft");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ExerciseResponseDto updateExercise(Long id, ExerciseUpsertRequestDto request) {
        Exercise exercise = findExercise(id);
        if (exercise.isPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unpublish the exercise before editing it");
        }
        exercises.findByExerciseKeyIgnoreCase(request.exerciseKey().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(_ -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise key already exists");
                });
        applyExercise(exercise, request);
        Exercise saved = exercises.save(exercise);
        audit.record("EXERCISE_UPDATED", "EXERCISE", id.toString(), "Exercise metadata updated");
        return toResponse(saved);
    }

    @Transactional
    @Override
    public ExerciseResponseDto setExercisePublished(Long id, boolean published) {
        Exercise exercise = findExercise(id);
        if (published && (exercise.getDescription() == null || exercise.getDescription().isBlank())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A description is required before publication");
        }
        exercise.setPublished(published);
        Exercise saved = exercises.save(exercise);
        audit.record(
                published ? "EXERCISE_PUBLISHED" : "EXERCISE_UNPUBLISHED",
                "EXERCISE",
                id.toString(),
                "Exercise published=" + published
        );
        return toResponse(saved);
    }

    @Transactional
    @Override
    public void deleteExercise(Long id) {
        Exercise exercise = findExercise(id);
        if (exercise.isPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unpublish the exercise before deleting it");
        }
        exercises.delete(exercise);
        audit.record("EXERCISE_DELETED", "EXERCISE", id.toString(), "Draft exercise deleted");
    }


    /*
     * WorkOut PLans
     * */

    @Transactional(readOnly = true)
    @Override
    public Page<WorkoutPlanResponseDto> getPublishedWorkoutPlansList(
            String query,
            String goal,
            String difficulty,
            Boolean published,
            Pageable pageable
    ) {
        return plans.search(
                normalizeSearchFilter(query),
                normalizeSearchFilter(goal),
                normalizeSearchFilter(difficulty),
                published,
                pageable
        ).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public WorkoutPlanResponseDto getPublishedWorkoutPlan(Long id, boolean publishedOnly) {
        WorkoutPlan plan = findPlan(id);
        if (publishedOnly && !plan.isPublished()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found");
        }
        return toResponse(plan);
    }

    @Transactional
    @Override
    public WorkoutPlanResponseDto createWorkoutPlan(WorkoutPlanUpsertRequestDto request) {
        if (plans.existsByPlanKeyIgnoreCase(request.planKey().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Workout plan key already exists");
        }
        WorkoutPlan plan = new WorkoutPlan();
        plan.setVersion(1);
        applyWorkoutPlan(plan, request);
        WorkoutPlan saved = plans.save(plan);
        audit.record("WORKOUT_PLAN_CREATED", "WORKOUT_PLAN", saved.getId().toString(), "Workout plan created as draft");
        return toResponse(saved);
    }

    @Transactional
    @Override
    public WorkoutPlanResponseDto updateWorkoutPlan(Long id, WorkoutPlanUpsertRequestDto request) {
        WorkoutPlan plan = findPlan(id);
        if (plan.isPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unpublish the workout plan before editing it");
        }
        plans.findByPlanKeyIgnoreCase(request.planKey().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(_ -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Workout plan key already exists");
                });
        applyWorkoutPlan(plan, request);
        plan.setVersion(plan.getVersion() + 1);
        WorkoutPlan saved = plans.save(plan);
        audit.record("WORKOUT_PLAN_UPDATED", "WORKOUT_PLAN", id.toString(), "Workout plan updated");
        return toResponse(saved);
    }

    @Transactional
    @Override
    public WorkoutPlanResponseDto setWorkoutPlanPublished(Long id, boolean published) {
        WorkoutPlan plan = findPlan(id);
        if (published) {
            if (plan.getDays().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "At least one workout day is required before publication");
            }
            boolean hasUnpublishedExercise = plan.getDays().stream()
                    .flatMap(day -> day.getPlanExercises().stream())
                    .anyMatch(item -> !item.getExercise().isPublished());
            if (hasUnpublishedExercise) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "All referenced exercises must be published first");
            }
        }
        plan.setPublished(published);
        WorkoutPlan saved = plans.save(plan);
        audit.record(
                published ? "WORKOUT_PLAN_PUBLISHED" : "WORKOUT_PLAN_UNPUBLISHED",
                "WORKOUT_PLAN",
                id.toString(),
                "Workout plan published=" + published
        );
        return toResponse(saved);
    }

    @Transactional
    @Override
    public void deleteWorkoutPlan(Long id) {
        WorkoutPlan plan = findPlan(id);
        if (plan.isPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unpublish the workout plan before deleting it");
        }
        plans.delete(plan);
        audit.record("WORKOUT_PLAN_DELETED", "WORKOUT_PLAN", id.toString(), "Draft workout plan deleted");
    }

    private void applyWorkoutPlan(WorkoutPlan plan, WorkoutPlanUpsertRequestDto request) {
        validateDays(request);
        plan.setPlanKey(request.planKey().trim());
        plan.setName(request.name().trim());
        plan.setDescription(normalize(request.description()));
        plan.setGoal(request.goal().trim().toUpperCase(Locale.ROOT));
        plan.setDifficulty(request.difficulty().trim().toUpperCase(Locale.ROOT));
        plan.setDurationWeeks((short) request.durationWeeks());
        plan.setDaysPerWeek((short) request.daysPerWeek());
        plan.setEstimatedSessionMinutes(request.estimatedSessionMinutes() == null
                ? null
                : request.estimatedSessionMinutes().shortValue());
        plan.setEquipmentRequirements(validateEquipmentJson(request.equipmentRequirementsJson()));
        plan.getDays().clear();

        Set<Long> exerciseIds = request.days().stream()
                .flatMap(day -> day.exercises() == null ? java.util.stream.Stream.empty() : day.exercises().stream())
                .map(WorkoutPlanUpsertRequestDto.PlanExercise::exerciseId)
                .collect(Collectors.toSet());
        Map<Long, Exercise> exerciseById = exercises.findAllById(exerciseIds).stream()
                .collect(Collectors.toMap(Exercise::getId, Function.identity()));
        if (exerciseById.size() != exerciseIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more referenced exercises do not exist");
        }

        for (WorkoutPlanUpsertRequestDto.WorkoutDay dayRequest : request.days()) {
            WorkoutPlanDay day = new WorkoutPlanDay();
            day.setPlan(plan);
            day.setDayNumber((short) dayRequest.dayNumber());
            day.setName(dayRequest.name().trim());
            day.setRestDay(dayRequest.restDay());
            plan.getDays().add(day);

            if (dayRequest.exercises() != null) {
                for (WorkoutPlanUpsertRequestDto.PlanExercise itemRequest : dayRequest.exercises()) {
                    WorkoutPlanExercise item = new WorkoutPlanExercise();
                    item.setWorkoutPlanDay(day);
                    item.setExercise(exerciseById.get(itemRequest.exerciseId()));
                    item.setSortOrder(itemRequest.sortOrder());
                    item.setTargetSets((short) itemRequest.targetSets());
                    item.setTargetRepsMin(itemRequest.targetRepsMin());
                    item.setTargetRepsMax(itemRequest.targetRepsMax());
                    item.setTargetDurationSeconds(itemRequest.targetDurationSeconds());
                    item.setRestSeconds(itemRequest.restSeconds());
                    item.setTargetWeightKg(itemRequest.targetWeightKg());
                    item.setNotes(normalize(itemRequest.notes()));
                    day.getPlanExercises().add(item);
                }
            }
        }
    }

    private void validateDays(WorkoutPlanUpsertRequestDto request) {
        Set<Long> dayNumbers = new HashSet<>();
        int trainingDays = 0;
        for (WorkoutPlanUpsertRequestDto.WorkoutDay day : request.days()) {
            if (day.dayNumber() > 7 || !dayNumbers.add(day.dayNumber())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Workout day numbers must be unique and within a seven-day week"
                );
            }
            List<WorkoutPlanUpsertRequestDto.PlanExercise> items = getPlanExercises(day);
            if (!day.restDay()) {
                trainingDays++;
            }
            Set<Integer> orders = new HashSet<>();
            for (WorkoutPlanUpsertRequestDto.PlanExercise item : items) {
                if (!orders.add(item.sortOrder())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exercise order must be unique within a workout day");
                }
                if (item.targetRepsMin() != null && item.targetRepsMax() != null
                        && item.targetRepsMin() > item.targetRepsMax()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum repetitions cannot exceed maximum repetitions");
                }
                if ((item.targetRepsMin() == null) != (item.targetRepsMax() == null)
                        && item.targetDurationSeconds() == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Provide both repetition bounds or a target duration"
                    );
                }
                if (item.targetRepsMin() == null && item.targetDurationSeconds() == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Every planned exercise requires repetitions or a target duration"
                    );
                }
            }
            if (trainingDays != request.daysPerWeek()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "daysPerWeek must equal the number of training days in the schedule"
                );
            }
        }
    }

    private static @NonNull List<WorkoutPlanUpsertRequestDto.PlanExercise> getPlanExercises(WorkoutPlanUpsertRequestDto.WorkoutDay day) {
        List<WorkoutPlanUpsertRequestDto.PlanExercise> items =
                day.exercises() == null ? List.of() : day.exercises();
        if (day.restDay() && !items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rest days cannot contain exercises");
        }
        if (!day.restDay() && items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Training days must contain at least one exercise");
        }
        return items;
    }

    private String validateEquipmentJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return "[]";
        }
        try {
            var node = objectMapper.readTree(raw);
            if (!node.isArray()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment requirements must be a JSON array");
            }
            return objectMapper.writeValueAsString(node);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid equipment requirements JSON", exception);
        }
    }

    private WorkoutPlan findPlan(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workout plan ID must be positive");
        }
        return plans.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found"));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeSearchFilter(String value) {
        return value == null ? "" : value.trim();
    }

    private WorkoutPlanResponseDto toResponse(WorkoutPlan plan) {
        List<WorkoutPlanResponseDto.Day> days = plan.getDays().stream()
                .sorted(java.util.Comparator.comparingInt(WorkoutPlanDay::getDayNumber))
                .map(day -> new WorkoutPlanResponseDto.Day(
                        day.getDayNumber(),
                        day.getName(),
                        day.isRestDay(),
                        day.getPlanExercises().stream()
                                .sorted(java.util.Comparator.comparingInt(WorkoutPlanExercise::getSortOrder))
                                .map(item -> new WorkoutPlanResponseDto.PlanExercise(
                                        item.getExercise().getId(),
                                        item.getExercise().getExerciseKey(),
                                        item.getExercise().getName(),
                                        item.getSortOrder(),
                                        item.getTargetSets(),
                                        item.getTargetRepsMin(),
                                        item.getTargetRepsMax(),
                                        item.getTargetDurationSeconds(),
                                        item.getRestSeconds(),
                                        item.getTargetWeightKg(),
                                        item.getNotes()
                                ))
                                .toList()
                ))
                .toList();
        return new WorkoutPlanResponseDto(
                plan.getId(),
                plan.getPlanKey(),
                plan.getName(),
                plan.getDescription(),
                plan.getGoal(),
                plan.getDifficulty(),
                plan.getDurationWeeks(),
                plan.getDaysPerWeek(),
                plan.getEstimatedSessionMinutes() == null ? null : plan.getEstimatedSessionMinutes().intValue(),
                plan.getEquipmentRequirements(),
                plan.isPublished(),
                plan.getVersion(),
                days,
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }



    /*
     * Helper
     * */

    private void applyExercise(Exercise exercise, ExerciseUpsertRequestDto request) {
        exercise.setExerciseKey(request.exerciseKey().trim());
        exercise.setName(request.name().trim());
        exercise.setDescription(trimToNull(request.description()));
        exercise.setInstructions(validateJson(request.instructionsJson(), true));
        exercise.setDifficulty(request.difficulty());
        exercise.setExerciseType(request.exerciseType());
        exercise.setMovementPattern(trimToNull(request.movementPattern()));
        exercise.setEquipment(request.equipment().trim());
        exercise.setUnilateral(request.unilateral());
        exercise.setSupportsAiTracking(request.supportsAiTracking());
        exercise.setTrackingConfig(validateJson(request.trackingConfigJson(), false));
    }

    private String validateJson(String raw, boolean arrayRequired) {
        if (raw == null || raw.isBlank()) {
            return arrayRequired ? "[]" : null;
        }

        try {
            JsonNode node = objectMapper.readTree(raw);

            if (node == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid JSON in exercise content"
                );
            }

            if (arrayRequired && !node.isArray()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Instructions must be a JSON array"
                );
            }

            return objectMapper.writeValueAsString(node);

        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid JSON in exercise content",
                    exception
            );
        }
    }

    private Exercise findExercise(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exercise ID must be positive");
        }
        return exercises.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));
    }

    private String currentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return auth.getName();
    }

    private ExerciseHomeResponseDto.RecentWorkout toRecentWorkout(UserWorkout w) {
        return new ExerciseHomeResponseDto.RecentWorkout(
                w.getId(),
                w.getWorkoutPlanId(),
                w.getStartedAt(),
                w.getCompletedAt(),
                w.getDurationSeconds(),
                w.getExerciseLogs().size()
        );
    }

    private ExerciseHomeResponseDto.ExerciseSummary toSummary(Exercise exercise) {
        String thumbnail = exercise.getMedia().stream()
                .min(Comparator.comparing((ExerciseMedia m) -> !m.isPrimary())
                        .thenComparingInt(ExerciseMedia::getSortOrder))
                .map(m -> m.getThumbnailUrl() != null ? m.getThumbnailUrl() : m.getMediaUrl())
                .orElse(null);
        return new ExerciseHomeResponseDto.ExerciseSummary(
                exercise.getId(),
                exercise.getExerciseKey(),
                exercise.getName(),
                exercise.getDifficulty(),
                exercise.getExerciseType(),
                exercise.getEquipment(),
                thumbnail
        );
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ExerciseResponseDto toResponse(Exercise exercise) {
        var media = exercise.getMedia().stream()
                .sorted(Comparator.comparingInt(ExerciseMedia::getSortOrder))
                .map(item -> new ExerciseResponseDto.MediaItem(
                        item.getId(),
                        item.getMediaType(),
                        item.getMediaUrl(),
                        item.getThumbnailUrl(),
                        item.getSortOrder(),
                        item.isPrimary()
                ))
                .toList();
        var muscles = exercise.getExerciseMuscles().stream()
                .map(item -> new ExerciseResponseDto.ExerciseMuscleList(
                        item.getRole(),
                        new ExerciseResponseDto.ExerciseMuscleList.MuscleGroupItem(item.getExerciseMuscleGroup().getMuscleKey(), item.getExerciseMuscleGroup().getName())
                ))
                .toList();
        return new ExerciseResponseDto(
                exercise.getId(),
                exercise.getExerciseKey(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getInstructions(),
                exercise.getDifficulty(),
                exercise.getExerciseType(),
                exercise.getMovementPattern(),
                exercise.getEquipment(),
                exercise.isUnilateral(),
                exercise.isSupportsAiTracking(),
                exercise.getTrackingConfig(),
                exercise.isPublished(),
                media,
                muscles,
                exercise.getCreatedAt(),
                exercise.getUpdatedAt()
        );
    }

}