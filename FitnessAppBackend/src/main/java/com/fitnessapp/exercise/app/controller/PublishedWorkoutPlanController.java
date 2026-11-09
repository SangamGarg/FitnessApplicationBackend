package com.fitnessapp.exercise.app.controller;

import com.fitnessapp.exercise.portal.WorkoutPlanService;
import com.fitnessapp.exercise.portal.dto.WorkoutPlanResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/app/catalog/workout-plans")
@Tag(name = "Mobile - Published Workout Plans", description = "Published workout plans for the mobile application")
public class PublishedWorkoutPlanController {

    private final WorkoutPlanService service;

    public PublishedWorkoutPlanController(WorkoutPlanService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List published workout plans")
    public ResponseEntity<Page<WorkoutPlanResponseDto>> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 100");
        }
        return ResponseEntity.ok(service.search(
                query,
                goal,
                difficulty,
                true,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a published workout plan")
    public ResponseEntity<WorkoutPlanResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id, true));
    }
}
