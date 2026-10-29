package com.fitnessapp.exercise.portal;

import tools.jackson.databind.ObjectMapper;
import com.fitnessapp.exercise.app.model.entity.entityNew.Exercise;
import com.fitnessapp.exercise.app.model.entity.entityNew.WorkoutPlan;
import com.fitnessapp.exercise.app.model.entity.entityNew.WorkoutPlanDay;
import com.fitnessapp.exercise.app.model.entity.entityNew.WorkoutPlanExercise;
import com.fitnessapp.exercise.app.repo.ExerciseRepository;
import com.fitnessapp.exercise.app.repo.WorkoutPlanRepository;
import com.fitnessapp.exercise.portal.dto.WorkoutPlanResponseDto;
import com.fitnessapp.exercise.portal.dto.WorkoutPlanUpsertRequestDto;
import com.fitnessapp.portal.audit.PortalAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutPlanService {

    private final WorkoutPlanRepository plans;
    private final ExerciseRepository exercises;
    private final ObjectMapper objectMapper;
    private final PortalAuditService audit;

    @Transactional(readOnly = true)
    public Page<WorkoutPlanResponseDto> search(
            String query,
            String goal,
            String difficulty,
            Boolean published,
            Pageable pageable
    ) {
        return plans.search(
                normalize(query),
                normalize(goal),
                normalize(difficulty),
                published,
                pageable
        ).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public WorkoutPlanResponseDto get(Long id, boolean publishedOnly) {
        WorkoutPlan plan = findPlan(id);
        if (publishedOnly && !plan.isPublished()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found");
        }
        return toResponse(plan);
    }

    @Transactional
    public WorkoutPlanResponseDto create(WorkoutPlanUpsertRequestDto request) {
        if (plans.existsByPlanKeyIgnoreCase(request.planKey().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Workout plan key already exists");
        }
        WorkoutPlan plan = new WorkoutPlan();
        plan.setVersion(1);
        apply(plan, request);
        WorkoutPlan saved = plans.save(plan);
        audit.record("WORKOUT_PLAN_CREATED", "WORKOUT_PLAN", saved.getId().toString(), "Workout plan created as draft");
        return toResponse(saved);
    }

    @Transactional
    public WorkoutPlanResponseDto update(Long id, WorkoutPlanUpsertRequestDto request) {
        WorkoutPlan plan = findPlan(id);
        if (plan.isPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unpublish the workout plan before editing it");
        }
        plans.findByPlanKeyIgnoreCase(request.planKey().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Workout plan key already exists");
                });
        apply(plan, request);
        plan.setVersion(plan.getVersion() + 1);
        WorkoutPlan saved = plans.save(plan);
        audit.record("WORKOUT_PLAN_UPDATED", "WORKOUT_PLAN", id.toString(), "Workout plan updated");
        return toResponse(saved);
    }

    @Transactional
    public WorkoutPlanResponseDto setPublished(Long id, boolean published) {
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
    public void deleteDraft(Long id) {
        WorkoutPlan plan = findPlan(id);
        if (plan.isPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unpublish the workout plan before deleting it");
        }
        plans.delete(plan);
        audit.record("WORKOUT_PLAN_DELETED", "WORKOUT_PLAN", id.toString(), "Draft workout plan deleted");
    }

    private void apply(WorkoutPlan plan, WorkoutPlanUpsertRequestDto request) {
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
            List<WorkoutPlanUpsertRequestDto.PlanExercise> items =
                    day.exercises() == null ? List.of() : day.exercises();
            if (day.restDay() && !items.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rest days cannot contain exercises");
            }
            if (!day.restDay() && items.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Training days must contain at least one exercise");
            }
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

    private WorkoutPlanResponseDto toResponse(WorkoutPlan plan) {
        List<WorkoutPlanResponseDto.Day> days = plan.getDays().stream()
                .sorted(java.util.Comparator.comparingInt(WorkoutPlanDay -> WorkoutPlanDay.getDayNumber()))
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
}
