package com.fitnessapp.exercise.app.controller;

import com.fitnessapp.exercise.app.model.dto.response.*;
import com.fitnessapp.exercise.app.service.WorkoutAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/v1/app/workouts/analytics")
@RequiredArgsConstructor
@Validated
@Tag(name = "Mobile - Workout Analytics", description = "Workout statistics, personal records and training progress")
public class WorkoutAnalyticsController {

    private final WorkoutAnalyticsService service;

    @GetMapping("/overview")
    @Operation(summary = "Get workout statistics for a date range")
    public ResponseEntity<WorkoutAnalyticsOverviewResponse> overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        return ResponseEntity.ok(service.overview(from, to, zone(timeZone)));
    }

    @GetMapping("/records")
    @Operation(summary = "Get all-time personal records for completed exercises")
    public ResponseEntity<List<PersonalRecordResponse>> records(
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        return ResponseEntity.ok(service.personalRecords(zone(timeZone)));
    }

    @GetMapping("/exercises/{exerciseId}/history")
    @Operation(summary = "Get paginated performance history for an exercise")
    public ResponseEntity<ExercisePerformanceHistoryResponse> exerciseHistory(
            @PathVariable @Min(1) Long exerciseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(service.exerciseHistoryEnvelope(exerciseId, from, to, zone(timeZone), page, size));
    }

    @GetMapping("/muscle-groups/volume")
    @Operation(summary = "Get primary-muscle training volume and frequency")
    public ResponseEntity<MuscleGroupVolumeResponse> muscleGroupVolume(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        return ResponseEntity.ok(service.muscleGroupVolume(from, to, zone(timeZone)));
    }

    @GetMapping("/trends")
    @Operation(summary = "Get weekly or monthly workout progress trends")
    public ResponseEntity<WorkoutTrendsResponse> trends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "WEEKLY") String period,
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        return ResponseEntity.ok(service.trends(from, to, zone(timeZone), period));
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
