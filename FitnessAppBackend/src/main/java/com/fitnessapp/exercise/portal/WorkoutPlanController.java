package com.fitnessapp.exercise.portal;

import com.fitnessapp.exercise.portal.dto.ExercisePublicationRequestDto;
import com.fitnessapp.exercise.portal.dto.WorkoutPlanResponseDto;
import com.fitnessapp.exercise.portal.dto.WorkoutPlanUpsertRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/portal/dev/workout-plans")
@Tag(name = "Developer - Workout Plans", description = "Manage, validate, and publish structured workout plans")
@SecurityRequirement(name = "bearerAuth")
public class WorkoutPlanController {

    private final WorkoutPlanService service;

    public WorkoutPlanController(WorkoutPlanService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Search workout plans")
    public ResponseEntity<Page<WorkoutPlanResponseDto>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Boolean published,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(service.search(query, goal, difficulty, published, pageable(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a workout plan")
    public ResponseEntity<WorkoutPlanResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id, false));
    }

    @PostMapping
    @Operation(summary = "Create a workout plan draft")
    public ResponseEntity<WorkoutPlanResponseDto> create(@Valid @RequestBody WorkoutPlanUpsertRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace an unpublished workout plan")
    public ResponseEntity<WorkoutPlanResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpsertRequestDto request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/publication")
    @Operation(summary = "Publish or unpublish a workout plan")
    public ResponseEntity<WorkoutPlanResponseDto> setPublished(
            @PathVariable Long id,
            @Valid @RequestBody ExercisePublicationRequestDto request
    ) {
        return ResponseEntity.ok(service.setPublished(id, request.published()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an unpublished workout plan")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteDraft(id);
        return ResponseEntity.noContent().build();
    }

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
