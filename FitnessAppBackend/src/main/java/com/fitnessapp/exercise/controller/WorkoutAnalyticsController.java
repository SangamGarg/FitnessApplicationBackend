package com.fitnessapp.exercise.controller;

import com.fitnessapp.exercise.model.dto.request.WorkoutExerciseAddRequest;
import com.fitnessapp.exercise.model.dto.request.WorkoutSetLogRequest;
import com.fitnessapp.exercise.model.dto.request.WorkoutStartRequest;
import com.fitnessapp.exercise.model.dto.response.*;
import com.fitnessapp.exercise.service.WorkoutAnalyticsService;
import com.fitnessapp.exercise.service.WorkoutSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_EXERCISE_ENDPOINT;

@RestController
@RequestMapping(APP_EXERCISE_ENDPOINT + "/workouts")
@RequiredArgsConstructor
@Validated
public class WorkoutAnalyticsController {

    private final WorkoutAnalyticsService workoutAnalyticsService;
    private final WorkoutSessionService workoutSessionService;

    @GetMapping("/analytics/overview")
    @Operation(summary = "Get workout statistics for a date range")
    public ResponseEntity<WorkoutAnalyticsOverviewResponse> overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        return ResponseEntity.ok(workoutAnalyticsService.overview(from, to, zone(timeZone)));
    }

    @GetMapping("/analytics/records")
    @Operation(summary = "Get all-time personal records for completed exercises")
    public ResponseEntity<List<PersonalRecordResponse>> records(
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        return ResponseEntity.ok(workoutAnalyticsService.personalRecords(zone(timeZone)));
    }

    @GetMapping("/analytics/exercises/{exerciseId}/history")
    @Operation(summary = "Get paginated performance history for an exercise")
    public ResponseEntity<ExercisePerformanceHistoryResponse> exerciseHistory(
            @PathVariable @Min(1) Long exerciseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(workoutAnalyticsService.exerciseHistoryEnvelope(exerciseId, from, to, zone(timeZone), page, size));
    }

    @GetMapping("/analytics/muscle-groups/volume")
    @Operation(summary = "Get primary-muscle training volume and frequency")
    public ResponseEntity<MuscleGroupVolumeResponse> muscleGroupVolume(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        return ResponseEntity.ok(workoutAnalyticsService.muscleGroupVolume(from, to, zone(timeZone)));
    }

    @GetMapping("/analytics/trends")
    @Operation(summary = "Get weekly or monthly workout progress trends")
    public ResponseEntity<WorkoutTrendsResponse> trends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "WEEKLY") String period,
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        return ResponseEntity.ok(workoutAnalyticsService.trends(from, to, zone(timeZone), period));
    }

    /**
     *
     * Workout Session
     */


    @PostMapping("/sessions")
    @Operation(summary = "Start a workout session")
    public ResponseEntity<WorkoutSessionResponse> start(@Valid @RequestBody WorkoutStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutSessionService.start(request));
    }

    @GetMapping("/sessions/{workoutId}")
    @Operation(summary = "Get a workout session")
    public ResponseEntity<WorkoutSessionResponse> get(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(workoutSessionService.get(workoutId));
    }

    @PostMapping("/sessions/{workoutId}/pause")
    @Operation(summary = "Pause an active workout")
    public ResponseEntity<WorkoutSessionResponse> pause(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(workoutSessionService.pause(workoutId));
    }

    @PostMapping("/sessions/{workoutId}/resume")
    @Operation(summary = "Resume a paused workout")
    public ResponseEntity<WorkoutSessionResponse> resume(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(workoutSessionService.resume(workoutId));
    }

    @PostMapping("/sessions/{workoutId}/exercises")
    @Operation(summary = "Add a published exercise to a workout")
    public ResponseEntity<WorkoutSessionResponse> addExercise(
            @PathVariable @Min(1) Long workoutId,
            @Valid @RequestBody WorkoutExerciseAddRequest request) {
        return ResponseEntity.ok(workoutSessionService.addExercise(workoutId, request));
    }

    @PostMapping("/sessions/{workoutId}/exercises/{exerciseLogId}/sets")
    @Operation(summary = "Record a completed exercise set")
    public ResponseEntity<WorkoutSessionResponse> logSet(
            @PathVariable @Min(1) Long workoutId,
            @PathVariable @Min(1) Long exerciseLogId,
            @Valid @RequestBody WorkoutSetLogRequest request) {
        return ResponseEntity.ok(workoutSessionService.logSet(workoutId, exerciseLogId, request));
    }

    @PostMapping("/sessions/{workoutId}/complete")
    @Operation(summary = "Complete a workout session")
    public ResponseEntity<WorkoutSessionResponse> complete(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(workoutSessionService.complete(workoutId));
    }

    @PostMapping("sessions/{workoutId}/cancel")
    @Operation(summary = "Cancel a workout session")
    public ResponseEntity<WorkoutSessionResponse> cancel(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(workoutSessionService.cancel(workoutId));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get workout history")
    public ResponseEntity<?> history(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(workoutSessionService.history(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"))));
    }

    private ZoneId zone(String value) {
        try {
            return ZoneId.of(value);
        } catch (DateTimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid timeZone. Use an IANA zone such as Asia/Kolkata.");
        }
    }
}
