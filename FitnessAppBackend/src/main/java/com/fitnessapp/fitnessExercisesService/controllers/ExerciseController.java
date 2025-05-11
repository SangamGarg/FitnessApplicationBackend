package com.fitnessapp.fitnessExercisesService.controllers;

import com.fitnessapp.fitnessExercisesService.exerciseServiceUtilities.AppConstantsExerciseService;
import com.fitnessapp.fitnessExercisesService.services.ExerciseService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstantsExerciseService.API_PREFIX)
@Validated
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        return exerciseService.getExerciseAllCategories();
    }

    @GetMapping("/categories/{categoryId}/levels")
    public ResponseEntity<?> getLevels(
            @PathVariable @NotNull @Min(1) Long categoryId) {
        return exerciseService.getExerciseLevelsByCategoryId(categoryId);
    }

    @GetMapping("/levels/{levelId}/subcategories")
    public ResponseEntity<?> getSubCategories(
            @PathVariable @NotNull @Min(1) Long levelId) {
        return exerciseService.getExerciseSubCategoriesByLevelId(levelId);
    }

    @GetMapping("/subcategories/{subCategoryId}/exercises")
    public ResponseEntity<?> getExercises(
            @PathVariable @NotNull @Min(1) Long subCategoryId) {
        return exerciseService.getExercisesBySubCategoryId(subCategoryId);
    }

    @GetMapping("/categories/suggestions")
    public ResponseEntity<?> getAllCategoriesSuggestions(
            @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return exerciseService.getExerciseAllCategoriesSuggestion(query);
    }

    @GetMapping("/subcategories/suggestions/{levelId}")
    public ResponseEntity<?> getSubCategoriesSuggestions(@PathVariable @NotNull @Min(1) Long levelId,
                                                         @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return exerciseService.getExerciseSubCategoriesSuggestion(levelId, query);
    }

    @GetMapping("/exercises/suggestions/{subCategoryId}")
    public ResponseEntity<?> getExercisesSuggestions(@PathVariable @NotNull @Min(1) Long subCategoryId,
                                                     @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return exerciseService.getExercisesDetailSuggestion(subCategoryId, query);
    }
}
