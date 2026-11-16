package com.fitnessapp.exercise.controller;

import com.fitnessapp.exercise.model.dto.request.ExerciseUpsertRequestDto;
import com.fitnessapp.exercise.model.dto.request.WorkoutPlanUpsertRequestDto;
import com.fitnessapp.exercise.model.dto.response.ExerciseResponseDto;
import com.fitnessapp.exercise.model.dto.response.WorkoutPlanResponseDto;
import com.fitnessapp.exercise.model.enums.ExerciseDifficulty;
import com.fitnessapp.exercise.model.enums.ExerciseType;
import com.fitnessapp.exercise.service.ExerciseAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_EXERCISE_ENDPOINT;

@RestController
@RequestMapping(PORTAL_EXERCISE_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Developer - Exercise Catalog", description = "Draft, edit, publish, and search structured exercise content")
public class ExercisePortalController {

    private final ExerciseAppService exerciseAppService;


    @GetMapping("/catalog")
    @Operation(summary = "Search exercises with pagination and filters")
    public ResponseEntity<Page<ExerciseResponseDto>> getPublishedExercisesList(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ExerciseDifficulty difficulty,
            @RequestParam(required = false) ExerciseType type,
            @RequestParam(required = false) Boolean published,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = pageable(page, size);
        return ResponseEntity.ok(exerciseAppService.getPublishedExercisesList(query, difficulty, type, published, pageable));
    }

    @GetMapping("/catalog/{id}")
    @Operation(summary = "Get an exercise including media metadata")
    public ResponseEntity<ExerciseResponseDto> getPublishedExercise(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseAppService.getPublishedExercise(id, false));
    }


    @PostMapping("/catalog")
    @Operation(summary = "Create an exercise draft")
    public ResponseEntity<ExerciseResponseDto> createExercise(@Valid @RequestBody ExerciseUpsertRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseAppService.createExercise(request));
    }

    @PutMapping("/catalog/{id}")
    @Operation(summary = "Replace exercise metadata")
    public ResponseEntity<ExerciseResponseDto> updateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseUpsertRequestDto request
    ) {
        return ResponseEntity.ok(exerciseAppService.updateExercise(id, request));
    }

    @PatchMapping("/catalog/{id}/publication")
    @Operation(summary = "Publish or unpublish an exercise")
    public ResponseEntity<ExerciseResponseDto> setExercisePublished(
            @PathVariable Long id,
            @RequestParam() boolean published
    ) {
        return ResponseEntity.ok(exerciseAppService.setExercisePublished(id, published));
    }

    @DeleteMapping("/catalog/{id}")
    @Operation(summary = "Delete an unpublished exercise draft")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long id) {
        exerciseAppService.deleteExercise(id);
        return ResponseEntity.noContent().build();
    }

    /*
     * WorkOut PLans
     * */

    @GetMapping("/workout-plans")
    @Operation(summary = "Search workout plans")
    public ResponseEntity<Page<WorkoutPlanResponseDto>> getPublishedWorkoutPlansList(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Boolean published,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(exerciseAppService.getPublishedWorkoutPlansList(query, goal, difficulty, published, pageable(page, size)));
    }

    @GetMapping("/workout-plans/{id}")
    @Operation(summary = "Get a workout plan")
    public ResponseEntity<WorkoutPlanResponseDto> getPublishedWorkoutPlan(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseAppService.getPublishedWorkoutPlan(id, false));
    }

    @PostMapping("/workout-plans")
    @Operation(summary = "Create a workout plan draft")
    public ResponseEntity<WorkoutPlanResponseDto> createWorkoutPlan(@Valid @RequestBody WorkoutPlanUpsertRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseAppService.createWorkoutPlan(request));
    }

    @PutMapping("/workout-plans/{id}")
    @Operation(summary = "Replace an unpublished workout plan")
    public ResponseEntity<WorkoutPlanResponseDto> updateWorkoutPlan(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpsertRequestDto request
    ) {
        return ResponseEntity.ok(exerciseAppService.updateWorkoutPlan(id, request));
    }

    @PatchMapping("/workout-plans/{id}/publication")
    @Operation(summary = "Publish or unpublish a workout plan")
    public ResponseEntity<WorkoutPlanResponseDto> setWorkoutPlanPublished(
            @PathVariable Long id,
            @RequestParam() boolean published
    ) {
        return ResponseEntity.ok(exerciseAppService.setWorkoutPlanPublished(id, published));
    }

    @DeleteMapping("/workout-plans/{id}")
    @Operation(summary = "Delete an unpublished workout plan")
    public ResponseEntity<Void> deleteWorkoutPlan(@PathVariable Long id) {
        exerciseAppService.deleteWorkoutPlan(id);
        return ResponseEntity.noContent().build();
    }

    /*
     * Pageable
     * */
    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
    }
}
