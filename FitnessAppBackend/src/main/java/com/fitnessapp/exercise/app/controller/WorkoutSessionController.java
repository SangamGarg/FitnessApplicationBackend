package com.fitnessapp.exercise.app.controller;

import com.fitnessapp.exercise.app.model.dto.request.*;
import com.fitnessapp.exercise.app.model.dto.response.WorkoutSessionResponse;
import com.fitnessapp.exercise.app.service.WorkoutSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/app/workouts/sessions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Mobile - Workout Sessions", description = "User workout session lifecycle and exercise set logging")
public class WorkoutSessionController {
    private final WorkoutSessionService service;

    @PostMapping
    @Operation(summary = "Start a workout session")
    public ResponseEntity<WorkoutSessionResponse> start(@Valid @RequestBody WorkoutStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.start(request));
    }

    @GetMapping("/{workoutId}")
    @Operation(summary = "Get a workout session")
    public ResponseEntity<WorkoutSessionResponse> get(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(service.get(workoutId));
    }

    @PostMapping("/{workoutId}/pause")
    @Operation(summary = "Pause an active workout")
    public ResponseEntity<WorkoutSessionResponse> pause(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(service.pause(workoutId));
    }

    @PostMapping("/{workoutId}/resume")
    @Operation(summary = "Resume a paused workout")
    public ResponseEntity<WorkoutSessionResponse> resume(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(service.resume(workoutId));
    }

    @PostMapping("/{workoutId}/exercises")
    @Operation(summary = "Add a published exercise to a workout")
    public ResponseEntity<WorkoutSessionResponse> addExercise(
            @PathVariable @Min(1) Long workoutId,
            @Valid @RequestBody WorkoutExerciseAddRequest request) {
        return ResponseEntity.ok(service.addExercise(workoutId, request));
    }

    @PostMapping("/{workoutId}/exercises/{exerciseLogId}/sets")
    @Operation(summary = "Record a completed exercise set")
    public ResponseEntity<WorkoutSessionResponse> logSet(
            @PathVariable @Min(1) Long workoutId,
            @PathVariable @Min(1) Long exerciseLogId,
            @Valid @RequestBody WorkoutSetLogRequest request) {
        return ResponseEntity.ok(service.logSet(workoutId, exerciseLogId, request));
    }

    @PostMapping("/{workoutId}/complete")
    @Operation(summary = "Complete a workout session")
    public ResponseEntity<WorkoutSessionResponse> complete(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(service.complete(workoutId));
    }

    @PostMapping("/{workoutId}/cancel")
    @Operation(summary = "Cancel a workout session")
    public ResponseEntity<WorkoutSessionResponse> cancel(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(service.cancel(workoutId));
    }

    @GetMapping
    @Operation(summary = "Get workout history")
    public ResponseEntity<?> history(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(service.history(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"))));
    }
}
