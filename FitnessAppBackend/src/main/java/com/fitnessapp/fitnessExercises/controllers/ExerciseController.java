package com.fitnessapp.fitnessExercises.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.fitnessExercises.exerciseServiceUtilities.AppConstantsExerciseService;
import com.fitnessapp.fitnessExercises.services.ExerciseService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstantsExerciseService.API_PREFIX)
@Validated
@Tag(name = "Exercise", description = "Endpoints for exercise")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @Operation(summary = "get All Categories", description = """
        Executes the get all categories operation.
        """)
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        return exerciseService.getExerciseAllCategories();
    }

    @Operation(summary = "get Levels", description = """
        Executes the get levels operation.
        """)
    @GetMapping("/categories/{categoryId}/levels")
    public ResponseEntity<?> getLevels(
            @PathVariable @NotNull @Min(1) Long categoryId) {
        return exerciseService.getExerciseLevelsByCategoryId(categoryId);
    }

    @Operation(summary = "get Sub Categories", description = """
        Executes the get sub categories operation.
        """)
    @GetMapping("/levels/{levelId}/subcategories")
    public ResponseEntity<?> getSubCategories(
            @PathVariable @NotNull @Min(1) Long levelId) {
        return exerciseService.getExerciseSubCategoriesByLevelId(levelId);
    }

    @Operation(summary = "get Exercises", description = """
        Executes the get exercises operation.
        """)
    @GetMapping("/subcategories/{subCategoryId}/exercises")
    public ResponseEntity<?> getExercises(
            @PathVariable @NotNull @Min(1) Long subCategoryId) {
        return exerciseService.getExercisesBySubCategoryId(subCategoryId);
    }

    @Operation(summary = "get All Categories Suggestions", description = """
        Executes the get all categories suggestions operation.
        """)
    @GetMapping("/categories/suggestions")
    public ResponseEntity<?> getAllCategoriesSuggestions(
            @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return exerciseService.getExerciseAllCategoriesSuggestion(query);
    }

    @Operation(summary = "get Sub Categories Suggestions", description = """
        Executes the get sub categories suggestions operation.
        """)
    @GetMapping("/subcategories/suggestions/{levelId}")
    public ResponseEntity<?> getSubCategoriesSuggestions(@PathVariable @NotNull @Min(1) Long levelId,
                                                         @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return exerciseService.getExerciseSubCategoriesSuggestion(levelId, query);
    }

    @Operation(summary = "get Exercises Suggestions", description = """
        Executes the get exercises suggestions operation.
        """)
    @GetMapping("/exercises/suggestions/{subCategoryId}")
    public ResponseEntity<?> getExercisesSuggestions(@PathVariable @NotNull @Min(1) Long subCategoryId,
                                                     @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return exerciseService.getExercisesDetailSuggestion(subCategoryId, query);
    }
}
