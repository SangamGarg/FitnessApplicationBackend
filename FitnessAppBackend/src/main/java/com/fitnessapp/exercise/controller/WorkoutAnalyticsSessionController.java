package com.fitnessapp.exercise.controller;

import com.fitnessapp.exercise.model.dto.request.WorkoutExerciseAddRequest;
import com.fitnessapp.exercise.model.dto.request.WorkoutSetLogRequest;
import com.fitnessapp.exercise.model.dto.request.WorkoutStartRequest;
import com.fitnessapp.exercise.model.dto.response.*;
import com.fitnessapp.exercise.service.WorkoutAnalyticsService;
import com.fitnessapp.exercise.service.WorkoutSessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
public class WorkoutAnalyticsSessionController {

    private final WorkoutAnalyticsService workoutAnalyticsService;
    private final WorkoutSessionService workoutSessionService;

    @GetMapping("/analytics/overview")
    public ResponseEntity<WorkoutAnalyticsOverviewResponse> overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        return ResponseEntity.ok(workoutAnalyticsService.overview(from, to, zone(timeZone)));
    }

    @GetMapping("/analytics/records")
    public ResponseEntity<List<PersonalRecordResponse>> records(
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        return ResponseEntity.ok(workoutAnalyticsService.personalRecords(zone(timeZone)));
    }

    @GetMapping("/analytics/exercises/{exerciseId}/history")
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
    public ResponseEntity<MuscleGroupVolumeResponse> muscleGroupVolume(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        return ResponseEntity.ok(workoutAnalyticsService.muscleGroupVolume(from, to, zone(timeZone)));
    }

    @GetMapping("/analytics/trends")
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
    public ResponseEntity<WorkoutSessionResponse> start(@Valid @RequestBody WorkoutStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutSessionService.start(request));
    }

    @GetMapping("/sessions/{workoutId}")
    public ResponseEntity<WorkoutSessionResponse> get(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(workoutSessionService.get(workoutId));
    }

    @PostMapping("/sessions/{workoutId}/pause")
    public ResponseEntity<WorkoutSessionResponse> pause(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(workoutSessionService.pause(workoutId));
    }

    @PostMapping("/sessions/{workoutId}/resume")
    public ResponseEntity<WorkoutSessionResponse> resume(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(workoutSessionService.resume(workoutId));
    }

    @PostMapping("/sessions/{workoutId}/exercises")
    public ResponseEntity<WorkoutSessionResponse> addExercise(
            @PathVariable @Min(1) Long workoutId,
            @Valid @RequestBody WorkoutExerciseAddRequest request) {
        return ResponseEntity.ok(workoutSessionService.addExercise(workoutId, request));
    }

    @PostMapping("/sessions/{workoutId}/exercises/{exerciseLogId}/sets")
    public ResponseEntity<WorkoutSessionResponse> logSet(
            @PathVariable @Min(1) Long workoutId,
            @PathVariable @Min(1) Long exerciseLogId,
            @Valid @RequestBody WorkoutSetLogRequest request) {
        return ResponseEntity.ok(workoutSessionService.logSet(workoutId, exerciseLogId, request));
    }

    @PostMapping("/sessions/{workoutId}/complete")
    public ResponseEntity<WorkoutSessionResponse> complete(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(workoutSessionService.complete(workoutId));
    }

    @PostMapping("sessions/{workoutId}/cancel")
    public ResponseEntity<WorkoutSessionResponse> cancel(@PathVariable @Min(1) Long workoutId) {
        return ResponseEntity.ok(workoutSessionService.cancel(workoutId));
    }

    @GetMapping("/sessions")
    public ResponseEntity<Page<WorkoutSessionResponse>> history(
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
