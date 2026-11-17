package com.fitnessapp.exercise.controller;

import com.fitnessapp.exercise.model.dto.response.ExerciseHomeResponseDto;
import com.fitnessapp.exercise.model.dto.response.ExerciseResponseDto;
import com.fitnessapp.exercise.model.dto.response.WorkoutPlanResponseDto;
import com.fitnessapp.exercise.model.enums.ExerciseDifficulty;
import com.fitnessapp.exercise.model.enums.ExerciseType;
import com.fitnessapp.exercise.service.ExerciseAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_EXERCISE_ENDPOINT;

@RestController
@RequestMapping(APP_EXERCISE_ENDPOINT)
@Validated
@RequiredArgsConstructor
public class ExerciseAppController {

    private final ExerciseAppService exerciseAppService;

    @GetMapping("/home")
    public ResponseEntity<ExerciseHomeResponseDto> getExerciseHome() {
        return exerciseAppService.getExerciseHome();
    }

    @GetMapping("/catalog")
    //List published exercises with filters
    public ResponseEntity<Page<ExerciseResponseDto>> getPublishedExercisesList(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ExerciseDifficulty difficulty,
            @RequestParam(required = false) ExerciseType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 100");
        }
        return ResponseEntity.ok(exerciseAppService.getPublishedExercisesList(
                query,
                difficulty,
                type,
                true,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        ));
    }

    @GetMapping("/catalog/by-muscle")
    //Search published exercises by muscle group (muscle key or name), optionally by role PRIMARY/SECONDARY
    public ResponseEntity<Page<ExerciseResponseDto>> searchExercisesByMuscleGroup(
            @RequestParam String muscle,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 100");
        }
        return ResponseEntity.ok(exerciseAppService.searchExercisesByMuscleGroup(
                muscle,
                role,
                true,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        ));
    }

    @GetMapping("/catalog/{id}")
    //Get a published exercise
    public ResponseEntity<ExerciseResponseDto> getPublishedExercise(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseAppService.getPublishedExercise(id, true));
    }


    /*
     * WorkOut PLans
     * */

    @GetMapping("/workout-plans")
    //List of published workout plans
    public ResponseEntity<Page<WorkoutPlanResponseDto>> getPublishedWorkoutPlansList(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 100");
        }
        return ResponseEntity.ok(exerciseAppService.getPublishedWorkoutPlansList(
                query,
                goal,
                difficulty,
                true,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        ));
    }

    @GetMapping("/workout-plans/{id}")
    //Get a published workout plan
    public ResponseEntity<WorkoutPlanResponseDto> getPublishedWorkoutPlan(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseAppService.getPublishedWorkoutPlan(id, true));
    }

}
