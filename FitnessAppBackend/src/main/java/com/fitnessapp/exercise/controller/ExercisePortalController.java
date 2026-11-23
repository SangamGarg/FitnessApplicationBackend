
package com.fitnessapp.exercise.controller;

import com.fitnessapp.exercise.model.dto.request.ExerciseUpsertRequestDto;
import com.fitnessapp.exercise.model.dto.request.CreateExerciseMuscleGroupRequestDto;
import com.fitnessapp.exercise.model.dto.request.UpdateExerciseMuscleGroupRequestDto;
import com.fitnessapp.exercise.model.dto.request.WorkoutPlanUpsertRequestDto;
import com.fitnessapp.exercise.model.dto.response.ExerciseResponseDto;
import com.fitnessapp.exercise.model.dto.response.ExerciseMuscleGroupResponseDto;
import com.fitnessapp.exercise.model.dto.response.WorkoutPlanResponseDto;
import com.fitnessapp.exercise.model.enums.ExerciseDifficulty;
import com.fitnessapp.exercise.model.enums.ExerciseType;
import com.fitnessapp.exercise.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_EXERCISE_ENDPOINT;

@RestController
@RequestMapping(PORTAL_DEV_EXERCISE_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
public class ExercisePortalController {

    private final ExerciseService exerciseService;

    @GetMapping("/muscle-groups")
    public ResponseEntity<List<ExerciseMuscleGroupResponseDto>> getExerciseMuscleGroups() {
        return ResponseEntity.ok(exerciseService.getExerciseMuscleGroups());
    }

    @PostMapping("/muscle-groups")
    public ResponseEntity<ExerciseMuscleGroupResponseDto> createExerciseMuscleGroup(
            @Valid @RequestBody CreateExerciseMuscleGroupRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(exerciseService.createExerciseMuscleGroup(request));
    }

    @PatchMapping("/muscle-groups/{id}")
    public ResponseEntity<ExerciseMuscleGroupResponseDto> updateExerciseMuscleGroup(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExerciseMuscleGroupRequestDto request
    ) {
        return ResponseEntity.ok(exerciseService.updateExerciseMuscleGroup(id, request));
    }

    @DeleteMapping("/muscle-groups/{id}")
    public ResponseEntity<Void> deleteExerciseMuscleGroup(@PathVariable Long id) {
        exerciseService.deleteExerciseMuscleGroup(id);
        return ResponseEntity.noContent().build();
    }

    /*
     * EXERCISE CATALOG
     */

    @GetMapping("/catalog")
    public ResponseEntity<Page<ExerciseResponseDto>> getPublishedExercisesList(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ExerciseDifficulty difficulty,
            @RequestParam(required = false) ExerciseType type,
            @RequestParam(required = false) Boolean published,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        log.info(
                "Fetching exercise catalog. query={}, difficulty={}, type={}, published={}, page={}, size={}",
                query, difficulty, type, published, page, size
        );

        Pageable pageable = pageable(page, size);

        Page<ExerciseResponseDto> response =
                exerciseService.getPublishedExercisesList(
                        query, difficulty, type, published, pageable
                );

        log.info(
                "Exercise catalog fetched successfully. totalElements={}, totalPages={}, currentPage={}",
                response.getTotalElements(),
                response.getTotalPages(),
                response.getNumber()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/catalog/{id}")
    public ResponseEntity<ExerciseResponseDto> getPublishedExercise(
            @PathVariable Long id
    ) {

        log.info("Fetching exercise details. exerciseId={}", id);

        ExerciseResponseDto response =
                exerciseService.getPublishedExercise(id, false);

        log.info("Exercise fetched successfully. exerciseId={}", id);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/catalog", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExerciseResponseDto> createExerciseWithMedia(
            @Valid @RequestPart("request") ExerciseUpsertRequestDto request,
            @RequestPart(value = "media", required = false) List<MultipartFile> mediaFiles
    ) {

        int mediaCount = mediaFiles == null ? 0 : mediaFiles.size();

        log.info(
                "Creating exercise. exerciseKey={}, name={}, mediaCount={}",
                request.exerciseKey(),
                request.name(),
                mediaCount
        );

        ExerciseResponseDto response =
                exerciseService.createExercise(request, mediaFiles);

        log.info(
                "Exercise created successfully. exerciseId={}, exerciseKey={}, mediaCount={}",
                response.id(),
                response.exerciseKey(),
                mediaCount
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/catalog/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExerciseResponseDto> updateExerciseWithMedia(
            @PathVariable Long id,
            @Valid @RequestPart("request") ExerciseUpsertRequestDto request,
            @RequestPart(value = "media", required = false) List<MultipartFile> mediaFiles
    ) {

        int mediaCount = mediaFiles == null ? 0 : mediaFiles.size();

        log.info(
                "Updating exercise. exerciseId={}, exerciseKey={}, mediaCount={}",
                id,
                request.exerciseKey(),
                mediaCount
        );

        ExerciseResponseDto response =
                exerciseService.updateExercise(id, request, mediaFiles);

        log.info(
                "Exercise updated successfully. exerciseId={}, exerciseKey={}, mediaCount={}",
                response.id(),
                response.exerciseKey(),
                mediaCount
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/catalog/{id}/publication")
    public ResponseEntity<ExerciseResponseDto> setExercisePublished(
            @PathVariable Long id,
            @RequestParam boolean published
    ) {

        log.info(
                "Updating exercise publication status. exerciseId={}, published={}",
                id,
                published
        );

        ExerciseResponseDto response =
                exerciseService.setExercisePublished(id, published);

        log.info(
                "Exercise publication status updated successfully. exerciseId={}, published={}",
                response.id(),
                response.published()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/catalog/{id}")
    public ResponseEntity<Void> deleteExercise(
            @PathVariable Long id
    ) {

        log.info("Deleting exercise. exerciseId={}", id);

        exerciseService.deleteExercise(id);

        log.info("Exercise deleted successfully. exerciseId={}", id);

        return ResponseEntity.noContent().build();
    }

    /*
     * WORKOUT PLANS
     */

    @GetMapping("/workout-plans")
    public ResponseEntity<Page<WorkoutPlanResponseDto>> getPublishedWorkoutPlansList(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Boolean published,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        log.info(
                "Fetching workout plans. query={}, goal={}, difficulty={}, published={}, page={}, size={}",
                query, goal, difficulty, published, page, size
        );

        Pageable pageable = pageable(page, size);

        Page<WorkoutPlanResponseDto> response =
                exerciseService.getPublishedWorkoutPlansList(
                        query, goal, difficulty, published, pageable
                );

        log.info(
                "Workout plans fetched successfully. totalElements={}, totalPages={}, currentPage={}",
                response.getTotalElements(),
                response.getTotalPages(),
                response.getNumber()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/workout-plans/{id}")
    public ResponseEntity<WorkoutPlanResponseDto> getPublishedWorkoutPlan(
            @PathVariable Long id
    ) {

        log.info("Fetching workout plan details. workoutPlanId={}", id);

        WorkoutPlanResponseDto response =
                exerciseService.getPublishedWorkoutPlan(id, false);

        log.info("Workout plan fetched successfully. workoutPlanId={}", id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/workout-plans")
    public ResponseEntity<WorkoutPlanResponseDto> createWorkoutPlan(
            @Valid @RequestBody WorkoutPlanUpsertRequestDto request
    ) {

        log.info(
                "Creating workout plan. planKey={}, name={}, durationWeeks={}, daysPerWeek={}",
                request.planKey(),
                request.name(),
                request.durationWeeks(),
                request.daysPerWeek()
        );

        WorkoutPlanResponseDto response =
                exerciseService.createWorkoutPlan(request);

        log.info(
                "Workout plan created successfully. workoutPlanId={}, planKey={}",
                response.id(),
                response.planKey()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/workout-plans/{id}")
    public ResponseEntity<WorkoutPlanResponseDto> updateWorkoutPlan(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpsertRequestDto request
    ) {

        log.info(
                "Updating workout plan. workoutPlanId={}, planKey={}, name={}",
                id,
                request.planKey(),
                request.name()
        );

        WorkoutPlanResponseDto response =
                exerciseService.updateWorkoutPlan(id, request);

        log.info(
                "Workout plan updated successfully. workoutPlanId={}, planKey={}",
                response.id(),
                response.planKey()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/workout-plans/{id}/publication")
    public ResponseEntity<WorkoutPlanResponseDto> setWorkoutPlanPublished(
            @PathVariable Long id,
            @RequestParam boolean published
    ) {

        log.info(
                "Updating workout plan publication status. workoutPlanId={}, published={}",
                id,
                published
        );

        WorkoutPlanResponseDto response =
                exerciseService.setWorkoutPlanPublished(id, published);

        log.info(
                "Workout plan publication status updated successfully. workoutPlanId={}, published={}",
                response.id(),
                response.published()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/workout-plans/{id}")
    public ResponseEntity<Void> deleteWorkoutPlan(
            @PathVariable Long id
    ) {

        log.info("Deleting workout plan. workoutPlanId={}", id);

        exerciseService.deleteWorkoutPlan(id);

        log.info("Workout plan deleted successfully. workoutPlanId={}", id);

        return ResponseEntity.noContent().build();
    }

    private Pageable pageable(int page, int size) {

        log.debug("Validating pagination parameters. page={}, size={}", page, size);

        if (page < 0 || size < 1 || size > 100) {

            log.warn(
                    "Invalid pagination parameters received. page={}, size={}",
                    page,
                    size
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "updatedAt")
        );
    }
}
