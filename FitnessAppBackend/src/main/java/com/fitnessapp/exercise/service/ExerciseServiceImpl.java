package com.fitnessapp.exercise.service;

import com.fitnessapp.assets.service.FileStorageService;
import com.fitnessapp.exercise.model.dto.request.CreateExerciseMuscleGroupRequestDto;
import com.fitnessapp.exercise.model.dto.request.ExerciseUpsertRequestDto;
import com.fitnessapp.exercise.model.dto.request.UpdateExerciseMuscleGroupRequestDto;
import com.fitnessapp.exercise.model.dto.request.WorkoutPlanUpsertRequestDto;
import com.fitnessapp.exercise.model.dto.response.ExerciseHomeResponseDto;
import com.fitnessapp.exercise.model.dto.response.ExerciseMuscleGroupResponseDto;
import com.fitnessapp.exercise.model.dto.response.ExerciseResponseDto;
import com.fitnessapp.exercise.model.dto.response.WorkoutPlanResponseDto;
import com.fitnessapp.exercise.model.entity.UserWorkout;
import com.fitnessapp.exercise.model.entity.exerciseEntity.*;
import com.fitnessapp.exercise.model.entity.workoutPlanEntity.WorkoutPlan;
import com.fitnessapp.exercise.model.entity.workoutPlanEntity.WorkoutPlanDay;
import com.fitnessapp.exercise.model.entity.workoutPlanEntity.WorkoutPlanExercise;
import com.fitnessapp.exercise.model.enums.ExerciseDifficulty;
import com.fitnessapp.exercise.model.enums.ExerciseType;
import com.fitnessapp.exercise.model.enums.MediaType;
import com.fitnessapp.exercise.model.enums.WorkoutStatus;
import com.fitnessapp.exercise.repo.*;
import com.fitnessapp.portal.audit.service.PortalAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exercises;
    private final ExerciseCategoryRepository categories;
    private final ExerciseSubCategoryRepository subCategories;
    private final ExerciseLevelRepository levels;
    private final ExerciseMuscleGroupRepository muscleGroups;
    private final WorkoutPlanRepository plans;
    private final WorkoutPlanExerciseRepository workoutPlanExercises;
    private final WorkoutExerciseLogRepository workoutExerciseLogs;
    private final UserWorkoutRepository workouts;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final PortalAuditService audit;

    private static final int HOME_FEATURED_PLAN_LIMIT = 5;
    private static final int MAX_EXERCISE_MEDIA_FILES = 10;

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseMuscleGroupResponseDto> getExerciseMuscleGroups() {
        return muscleGroups.findAll(Sort.by(Sort.Direction.ASC, "name", "muscleKey")).stream()
                .map(this::toMuscleGroupResponse)
                .toList();
    }

    @Override
    @Transactional
    public ExerciseMuscleGroupResponseDto createExerciseMuscleGroup(
            CreateExerciseMuscleGroupRequestDto request) {
        String muscleKey = request.muscleKey().trim().toLowerCase(Locale.ROOT);
        String name = request.name().trim();
        if (muscleGroups.existsByMuscleKeyIgnoreCase(muscleKey)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Muscle group key already exists");
        }

        ExerciseMuscleGroup muscleGroup = new ExerciseMuscleGroup();
        muscleGroup.setMuscleKey(muscleKey);
        muscleGroup.setName(name);
        ExerciseMuscleGroup saved = muscleGroups.save(muscleGroup);
        audit.record(
                "EXERCISE_MUSCLE_GROUP_CREATED",
                "EXERCISE_MUSCLE_GROUP",
                saved.getId().toString(),
                "Exercise muscle group created"
        );
        return toMuscleGroupResponse(saved);
    }

    @Override
    @Transactional
    public ExerciseMuscleGroupResponseDto updateExerciseMuscleGroup(
            Long id, UpdateExerciseMuscleGroupRequestDto request) {
        if (request.muscleKey() == null && request.name() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field must be provided");
        }

        ExerciseMuscleGroup muscleGroup = muscleGroups.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Muscle group not found"));

        if (request.muscleKey() != null) {
            String muscleKey = request.muscleKey().trim().toLowerCase(Locale.ROOT);
            if (muscleKey.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Muscle group key must not be blank");
            }
            if (!muscleGroup.getMuscleKey().equalsIgnoreCase(muscleKey)
                    && muscleGroups.existsByMuscleKeyIgnoreCase(muscleKey)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Muscle group key already exists");
            }
            muscleGroup.setMuscleKey(muscleKey);
        }

        if (request.name() != null) {
            String name = request.name().trim();
            if (name.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Muscle group name must not be blank");
            }
            muscleGroup.setName(name);
        }

        ExerciseMuscleGroup saved = muscleGroups.save(muscleGroup);
        audit.record(
                "EXERCISE_MUSCLE_GROUP_UPDATED",
                "EXERCISE_MUSCLE_GROUP",
                saved.getId().toString(),
                "Exercise muscle group updated"
        );
        return toMuscleGroupResponse(saved);
    }

    @Override
    @Transactional
    public void deleteExerciseMuscleGroup(Long id) {
        ExerciseMuscleGroup muscleGroup = muscleGroups.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Muscle group not found"));
        if (muscleGroups.isAssignedToExercise(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Muscle group assigned to exercises cannot be deleted"
            );
        }

        muscleGroups.delete(muscleGroup);
        audit.record(
                "EXERCISE_MUSCLE_GROUP_DELETED",
                "EXERCISE_MUSCLE_GROUP",
                id.toString(),
                "Exercise muscle group deleted"
        );
    }

    private ExerciseMuscleGroupResponseDto toMuscleGroupResponse(ExerciseMuscleGroup muscleGroup) {
        return new ExerciseMuscleGroupResponseDto(
                muscleGroup.getId(),
                muscleGroup.getMuscleKey(),
                muscleGroup.getName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ExerciseHomeResponseDto> getExerciseHome() {
        log.debug("Building exercise home response");
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
        log.debug("Exercise home personalization requested; authenticated={}", userId != null);
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

        log.info("Exercise home response assembled: exercises={}, muscleGroups={}, featuredPlans={}, personalized={}", totalExercises, muscleGroups.size(), featuredPlans.size(), userId != null);
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
        return getPublishedExercisesList(query, difficulty, type, null, null, published, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseResponseDto> getPublishedExercisesList(
            String query,
            ExerciseDifficulty difficulty,
            ExerciseType type,
            String muscle,
            String role,
            Boolean published,
            Pageable pageable
    ) {
        log.debug("Searching exercises: difficulty={}, type={}, published={}, muscle={}, role={}, page={}", difficulty, type, published, muscle, role, pageable);
        String normalizedQuery = query == null || query.isBlank()
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);
        String normalizedMuscle = muscle == null || muscle.isBlank()
                ? ""
                : muscle.trim().toLowerCase(Locale.ROOT);
        String normalizedRole = normalizeMuscleRole(role);
        return exercises.search(normalizedQuery, difficulty, type, normalizedMuscle, normalizedRole, published, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseResponseDto getPublishedExercise(Long id, boolean publishedOnly) {
        log.debug("Retrieving exercise id={}, publishedOnly={}", id, publishedOnly);
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
        log.debug("Searching exercises by muscle group: muscle={}, role={}, published={}", muscle, role, published);
        if (muscle == null || muscle.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Muscle group is required");
        }
        String normalizedRole = normalizeMuscleRole(role);
        return exercises.searchByMuscleGroup(
                muscle.trim().toLowerCase(Locale.ROOT),
                normalizedRole,
                published,
                pageable
        ).map(this::toResponse);
    }


    @Transactional
    @Override
    public ExerciseResponseDto createExercise(
            ExerciseUpsertRequestDto request, List<MultipartFile> mediaFiles) {
        log.info("Creating exercise key={}, name={}, mediaFiles={}", request.exerciseKey(), request.name(), mediaFiles == null ? 0 : mediaFiles.size());
        if (exercises.existsByExerciseKeyIgnoreCase(request.exerciseKey().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise key already exists");
        }
        Exercise exercise = new Exercise();
        applyExercise(exercise, request);
        addExerciseMedia(exercise, mediaFiles);
        Exercise saved = exercises.save(exercise);
        log.info("Exercise created id={}, key={}, mediaCount={}", saved.getId(), saved.getExerciseKey(), saved.getMedia().size());
        audit.record("EXERCISE_CREATED", "EXERCISE", saved.getId().toString(), "Exercise created as draft");
        return toResponse(saved);
    }


    @Override
    @Transactional
    public ExerciseResponseDto updateExercise(
            Long id, ExerciseUpsertRequestDto request, List<MultipartFile> mediaFiles) {
        log.info("Updating exercise id={}, key={}, incomingMedia={}", id, request.exerciseKey(), mediaFiles == null ? 0 : mediaFiles.size());
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
        updateExerciseMedia(exercise, mediaFiles, request.clearMedia());
        Exercise saved = exercises.save(exercise);
        log.info("Exercise updated id={}, key={}, mediaCount={}", saved.getId(), saved.getExerciseKey(), saved.getMedia().size());
        audit.record("EXERCISE_UPDATED", "EXERCISE", id.toString(), "Exercise metadata updated");
        return toResponse(saved);
    }

    @Transactional
    @Override
    public ExerciseResponseDto setExercisePublished(Long id, boolean published) {
        log.info("Setting exercise publication id={}, published={}", id, published);
        Exercise exercise = findExercise(id);
        if (published && (exercise.getDescription() == null || exercise.getDescription().isBlank())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A description is required before publication");
        }
        exercise.setPublished(published);
        Exercise saved = exercises.save(exercise);
        log.info("Exercise publication updated id={}, published={}", id, saved.isPublished());
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
        log.info("Deleting exercise id={}", id);
        Exercise exercise = findExercise(id);
        if (exercise.isPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unpublish the exercise before deleting it");
        }
        if (workoutPlanExercises.existsByExercise_Id(id) || workoutExerciseLogs.existsByExercise_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exercise cannot be deleted because it is referenced by a workout plan or workout history"
            );
        }
        List<String> mediaUrls = exercise.getMedia().stream()
                .map(ExerciseMedia::getMediaUrl)
                .toList();
        exercises.delete(exercise);
        log.info("Deleted exercise id={}, mediaFilesScheduledForCleanup={}", id, mediaUrls.size());
        scheduleMediaCleanup(mediaUrls, List.of());
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
        log.debug("Searching workout plans query={}, goal={}, difficulty={}, published={}, page={}", query, goal, difficulty, published, pageable);
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
        log.debug("Retrieving workout plan id={}, publishedOnly={}", id, publishedOnly);
        WorkoutPlan plan = findPlan(id);
        if (publishedOnly && !plan.isPublished()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found");
        }
        return toResponse(plan);
    }

    @Transactional
    @Override
    public WorkoutPlanResponseDto createWorkoutPlan(WorkoutPlanUpsertRequestDto request) {
        log.info("Creating workout plan key={}, name={}, days={}", request.planKey(), request.name(), request.days() == null ? 0 : request.days().size());
        if (plans.existsByPlanKeyIgnoreCase(request.planKey().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Workout plan key already exists");
        }
        WorkoutPlan plan = new WorkoutPlan();
        plan.setVersion(1);
        applyWorkoutPlan(plan, request);
        WorkoutPlan saved = plans.save(plan);
        log.info("Workout plan created id={}, key={}, version={}", saved.getId(), saved.getPlanKey(), saved.getVersion());
        audit.record("WORKOUT_PLAN_CREATED", "WORKOUT_PLAN", saved.getId().toString(), "Workout plan created as draft");
        return toResponse(saved);
    }

    @Transactional
    @Override
    public WorkoutPlanResponseDto updateWorkoutPlan(Long id, WorkoutPlanUpsertRequestDto request) {
        log.info("Updating workout plan id={}, key={}", id, request.planKey());
        WorkoutPlan plan = findPlan(id);
        if (plan.isPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unpublish the workout plan before editing it");
        }
        plans.findByPlanKeyIgnoreCase(request.planKey().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(_ -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Workout plan key already exists");
                });
        plan.getDays().clear();
        plans.flush();
        applyWorkoutPlan(plan, request);
        plan.setVersion(plan.getVersion() + 1);
        WorkoutPlan saved = plans.save(plan);
        log.info("Workout plan updated id={}, key={}, version={}", saved.getId(), saved.getPlanKey(), saved.getVersion());
        audit.record("WORKOUT_PLAN_UPDATED", "WORKOUT_PLAN", id.toString(), "Workout plan updated");
        return toResponse(saved);
    }

    @Transactional
    @Override
    public WorkoutPlanResponseDto setWorkoutPlanPublished(Long id, boolean published) {
        log.info("Setting workout plan publication id={}, published={}", id, published);
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
        log.info("Workout plan publication updated id={}, published={}", id, saved.isPublished());
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
        log.info("Deleting workout plan id={}", id);
        WorkoutPlan plan = findPlan(id);
        if (plan.isPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unpublish the workout plan before deleting it");
        }
        plans.delete(plan);
        log.info("Deleted workout plan id={}", id);
        audit.record("WORKOUT_PLAN_DELETED", "WORKOUT_PLAN", id.toString(), "Draft workout plan deleted");
    }

    private void applyWorkoutPlan(WorkoutPlan plan, WorkoutPlanUpsertRequestDto request) {
        log.debug("Applying workout plan changes key={}, days={}", request.planKey(), request.days() == null ? 0 : request.days().size());
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
        log.debug("Workout plan references {} distinct exercise IDs", exerciseIds.size());
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
        log.debug("Validating workout plan day structure");
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
            log.warn("Rejected invalid workout plan ID: {}", id);
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
                        day.getId(),
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
        exercise.setLevel(resolveLevel(request.difficulty()));
        exercise.setExerciseType(request.exerciseType());
        exercise.setMovementPattern(trimToNull(request.movementPattern()));
        exercise.setEquipment(request.equipment().trim());
        exercise.setUnilateral(request.unilateral());
        exercise.setSupportsAiTracking(request.supportsAiTracking());
        exercise.setTrackingConfig(validateJson(request.trackingConfigJson(), false));
        if (request.muscles() != null) {
            updateExerciseMuscles(exercise, request.muscles());
        }
        applyClassification(exercise, request);
        applyExerciseDetail(exercise, request);
    }

    private ExerciseLevel resolveLevel(ExerciseDifficulty difficulty) {
        String levelKey = difficulty.name();
        return levels.findByLevelKeyIgnoreCase(levelKey).orElseGet(() -> {
            ExerciseLevel level = new ExerciseLevel();
            level.setLevelKey(levelKey);
            level.setName(toDisplayName(levelKey));
            level.setSortOrder(difficulty.ordinal() + 1);
            return levels.save(level);
        });
    }

    private void applyClassification(Exercise exercise, ExerciseUpsertRequestDto request) {
        if (request.clearClassification()
                && (request.category() != null || request.subCategory() != null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category assignments cannot be supplied when clearClassification is true"
            );
        }
        if (request.clearClassification()) {
            exercise.setCategory(null);
            exercise.setSubCategory(null);
            return;
        }

        boolean categoryChanged = false;
        if (request.category() != null) {
            ExerciseCategory category = resolveCategory(request.category());
            categoryChanged = exercise.getCategory() == null
                    || !Objects.equals(exercise.getCategory().getId(), category.getId());
            exercise.setCategory(category);
        }

        if (request.subCategory() != null) {
            ExerciseCategory category = exercise.getCategory();
            if (category == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A category is required when assigning a subcategory"
                );
            }
            exercise.setSubCategory(resolveSubCategory(category, request.subCategory()));
        } else if (categoryChanged && exercise.getSubCategory() != null
                && !Objects.equals(exercise.getSubCategory().getCategory().getId(), exercise.getCategory().getId())) {
            exercise.setSubCategory(null);
        }
    }

    private ExerciseCategory resolveCategory(ExerciseUpsertRequestDto.CategoryAssignment assignment) {
        String key = assignment.categoryKey().trim();
        String name = assignment.name().trim();
        log.debug("Resolving exercise category key={}", key);
        ExerciseCategory category = categories.findByCategoryKeyIgnoreCase(key).orElseGet(() -> {
            ExerciseCategory created = new ExerciseCategory();
            created.setCategoryKey(key);
            created.setName(name);
            return categories.save(created);
        });
        if (!category.getName().equalsIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category key is already associated with a different name"
            );
        }
        if (!category.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inactive categories cannot be assigned");
        }
        return category;
    }

    private ExerciseSubCategory resolveSubCategory(
            ExerciseCategory category, ExerciseUpsertRequestDto.SubCategoryAssignment assignment) {
        String key = assignment.subCategoryKey().trim();
        String name = assignment.name().trim();
        log.debug("Resolving exercise subcategory key={} under category={}", key, category.getCategoryKey());
        ExerciseSubCategory subCategory =
                subCategories.findByCategoryAndSubCategoryKeyIgnoreCase(category, key).orElseGet(() -> {
                    ExerciseSubCategory created = new ExerciseSubCategory();
                    created.setCategory(category);
                    created.setSubCategoryKey(key);
                    created.setName(name);
                    return subCategories.save(created);
                });
        if (!subCategory.getName().equalsIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Subcategory key is already associated with a different name"
            );
        }
        if (!subCategory.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inactive subcategories cannot be assigned");
        }
        return subCategory;
    }

    private void applyExerciseDetail(Exercise exercise, ExerciseUpsertRequestDto request) {
        if (request.clearDetail() && request.detail() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Exercise details cannot be supplied when clearDetail is true"
            );
        }
        if (request.clearDetail()) {
            exercise.setDetail(null);
            return;
        }
        if (request.detail() == null) {
            return;
        }

        ExerciseUpsertRequestDto.ExerciseDetailRequest input = request.detail();
        String setup = trimToNull(input.setup());
        String benefits = trimToNull(input.benefits());
        String safetyNotes = trimToNull(input.safetyNotes());
        String commonMistakes = trimToNull(input.commonMistakes());
        if (setup == null && benefits == null && safetyNotes == null && commonMistakes == null) {
            exercise.setDetail(null);
            return;
        }

        ExerciseDetail detail = exercise.getDetail();
        if (detail == null) {
            detail = new ExerciseDetail();
            detail.setExercise(exercise);
            exercise.setDetail(detail);
        }
        detail.setSetup(setup);
        detail.setBenefits(benefits);
        detail.setSafetyNotes(safetyNotes);
        detail.setCommonMistakes(commonMistakes);
    }

    private String toDisplayName(String enumKey) {
        String normalized = enumKey.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private void updateExerciseMuscles(
            Exercise exercise, List<ExerciseUpsertRequestDto.MuscleGroupAssignment> assignments) {
        log.debug("Updating exercise muscle assignments count={}", assignments.size());
        Map<String, String> rolesByKey = new LinkedHashMap<>();
        int primaryCount = 0;
        for (ExerciseUpsertRequestDto.MuscleGroupAssignment assignment : assignments) {
            String muscleKey = assignment.muscleKey().trim();
            String normalizedKey = muscleKey.toLowerCase(Locale.ROOT);
            String role = assignment.role().trim().toUpperCase(Locale.ROOT);
            if (!role.equals("PRIMARY") && !role.equals("SECONDARY")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Muscle role must be PRIMARY or SECONDARY");
            }
            if (rolesByKey.putIfAbsent(normalizedKey, role) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Muscle groups must not be duplicated");
            }
            if (role.equals("PRIMARY")) {
                primaryCount++;
            }
        }
        if (primaryCount > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only one primary muscle group is allowed");
        }
        if (rolesByKey.isEmpty()) {
            exercise.getExerciseMuscles().clear();
            return;
        }

        List<ExerciseMuscleGroup> matchedGroups =
                muscleGroups.findByMuscleKeysIgnoreCase(rolesByKey.keySet());
        Map<String, ExerciseMuscleGroup> groupsByKey = matchedGroups.stream()
                .collect(Collectors.toMap(
                        group -> group.getMuscleKey().toLowerCase(Locale.ROOT),
                        Function.identity()
                ));
        if (groupsByKey.size() != rolesByKey.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more muscle groups / Muscle Keys do not exist");
        }

        exercise.getExerciseMuscles().clear();
        rolesByKey.forEach((key, role) -> {
            ExerciseMuscle muscle = new ExerciseMuscle();
            muscle.setExercise(exercise);
            muscle.setExerciseMuscleGroup(groupsByKey.get(key));
            muscle.setRole(role);
            exercise.getExerciseMuscles().add(muscle);
        });
    }

    private void addExerciseMedia(Exercise exercise, List<MultipartFile> mediaFiles) {
        List<MultipartFile> files = mediaFiles == null ? List.of() : mediaFiles;
        log.debug("Uploading exercise media files count={}", files.size());
        validateMediaFileCount(files);
        List<String> uploadedUrls = new ArrayList<>();
        try {
            for (int index = 0; index < files.size(); index++) {
                MultipartFile file = files.get(index);
                String mediaUrl = fileStorageService.uploadFile(file, "exercise_media");
                uploadedUrls.add(mediaUrl);
                exercise.getMedia().add(createExerciseMedia(exercise, file, mediaUrl, index));
            }
        } catch (RuntimeException exception) {
            cleanupUploadedFiles(uploadedUrls, exception);
            throw exception;
        }
        scheduleMediaCleanup(List.of(), uploadedUrls);
    }

    private void updateExerciseMedia(
            Exercise exercise, List<MultipartFile> mediaFiles, boolean clearMedia) {
        List<MultipartFile> files = mediaFiles == null ? List.of() : mediaFiles;
        log.debug("Updating exercise media incomingCount={}, clearMedia={}", files.size(), clearMedia);
        validateMediaFileCount(files);
        if (files.isEmpty() && !clearMedia) {
            return;
        }

        List<String> previousUrls = exercise.getMedia().stream()
                .map(ExerciseMedia::getMediaUrl)
                .toList();
        List<String> uploadedUrls = new ArrayList<>();
        try {
            for (int index = 0; index < files.size(); index++) {
                MultipartFile file = files.get(index);
                String mediaUrl = fileStorageService.uploadFile(file, "exercise_media");
                uploadedUrls.add(mediaUrl);
                exercise.getMedia().add(createExerciseMedia(exercise, file, mediaUrl, index));
            }
        } catch (RuntimeException exception) {
            cleanupUploadedFiles(uploadedUrls, exception);
            throw exception;
        }

        exercise.getMedia().removeIf(media -> previousUrls.contains(media.getMediaUrl()));
        scheduleMediaCleanup(previousUrls, uploadedUrls);
    }

    private ExerciseMedia createExerciseMedia(
            Exercise exercise, MultipartFile file, String mediaUrl, int sortOrder) {
        ExerciseMedia media = new ExerciseMedia();
        media.setExercise(exercise);
        media.setMediaType(isGif(file) ? MediaType.GIF : MediaType.IMAGE);
        media.setMediaUrl(mediaUrl);
        media.setSortOrder(sortOrder);
        media.setPrimary(sortOrder == 0);
        return media;
    }

    private void validateMediaFileCount(List<MultipartFile> mediaFiles) {
        if (mediaFiles.size() > MAX_EXERCISE_MEDIA_FILES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "An exercise can contain at most " + MAX_EXERCISE_MEDIA_FILES + " media files"
            );
        }
    }

    private void cleanupUploadedFiles(List<String> uploadedUrls, RuntimeException uploadException) {
        for (String uploadedUrl : uploadedUrls) {
            try {
                fileStorageService.deleteFileByUrl(uploadedUrl);
            } catch (RuntimeException cleanupException) {
                log.warn("Failed to clean up partially uploaded exercise media", cleanupException);
                uploadException.addSuppressed(cleanupException);
            }
        }
    }

    private boolean isGif(MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            return content.length >= 6
                    && (new String(content, 0, 6, java.nio.charset.StandardCharsets.US_ASCII).equals("GIF87a")
                    || new String(content, 0, 6, java.nio.charset.StandardCharsets.US_ASCII).equals("GIF89a"));
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Unable to read exercise media file", exception);
        }
    }

    private void scheduleMediaCleanup(List<String> previousUrls, List<String> uploadedUrls) {
        log.debug("Scheduling media cleanup: oldCount={}, newlyUploadedCount={}", previousUrls.size(), uploadedUrls.size());
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Exercise media changes require an active transaction");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                List<String> cleanupUrls = status == STATUS_COMMITTED
                        ? previousUrls
                        : status == STATUS_ROLLED_BACK ? uploadedUrls : List.of();
                cleanupUrls.forEach(url -> {
                    try {
                        fileStorageService.deleteFileByUrl(url);
                        log.debug("Media cleanup completed after transaction status={}", status);
                    } catch (RuntimeException exception) {
                        log.error("Media cleanup failed after transaction status={}", status, exception);
                    }
                });
            }
        });
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
            log.warn("Rejected invalid exercise ID: {}", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exercise ID must be positive");
        }
        return exercises.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));
    }

    private String normalizeMuscleRole(String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (!normalized.isEmpty() && !normalized.equals("PRIMARY") && !normalized.equals("SECONDARY")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be PRIMARY or SECONDARY");
        }
        return normalized;
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
                .sorted(Comparator.comparing((ExerciseMuscle item) -> !item.getRole().equalsIgnoreCase("PRIMARY"))
                        .thenComparing(item -> item.getExerciseMuscleGroup().getName(), String.CASE_INSENSITIVE_ORDER))
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
                exercise.getCategory() == null
                        ? null
                        : new ExerciseResponseDto.CategoryItem(
                        exercise.getCategory().getCategoryKey(),
                        exercise.getCategory().getName()),
                exercise.getSubCategory() == null
                        ? null
                        : new ExerciseResponseDto.SubCategoryItem(
                        exercise.getSubCategory().getSubCategoryKey(),
                        exercise.getSubCategory().getName()),
                exercise.getLevel() == null
                        ? null
                        : new ExerciseResponseDto.LevelItem(
                        exercise.getLevel().getLevelKey(),
                        exercise.getLevel().getName(),
                        exercise.getLevel().getSortOrder()),
                exercise.getDetail() == null
                        ? null
                        : new ExerciseResponseDto.ExerciseDetailItem(
                        exercise.getDetail().getSetup(),
                        exercise.getDetail().getBenefits(),
                        exercise.getDetail().getSafetyNotes(),
                        exercise.getDetail().getCommonMistakes()),
                exercise.getCreatedAt(),
                exercise.getUpdatedAt()
        );
    }

}
