package com.fitnessapp.exercise.app.controller;

import com.fitnessapp.exercise.app.model.ExerciseDifficulty;
import com.fitnessapp.exercise.app.model.ExerciseType;
import com.fitnessapp.exercise.portal.ExerciseCatalogService;
import com.fitnessapp.exercise.portal.dto.ExerciseResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/app/catalog/exercises")
@Tag(name = "Mobile - Published Exercises", description = "Published exercise content for the mobile application")
public class PublishedExerciseController {

    private final ExerciseCatalogService service;

    public PublishedExerciseController(ExerciseCatalogService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List published exercises with filters")
    public ResponseEntity<Page<ExerciseResponseDto>> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ExerciseDifficulty difficulty,
            @RequestParam(required = false) ExerciseType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 100");
        }
        return ResponseEntity.ok(service.search(
                query,
                difficulty,
                type,
                true,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a published exercise")
    public ResponseEntity<ExerciseResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id, true));
    }
}
