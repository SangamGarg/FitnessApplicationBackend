package com.fitnessapp.exercise.app.controller;

import com.fitnessapp.exercise.app.service.ExerciseAppService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_EXERCISE_ENDPOINT;

@RestController
@RequestMapping(APP_EXERCISE_ENDPOINT)
@Validated
@RequiredArgsConstructor
public class ExerciseAppController {

    private final ExerciseAppService exerciseAppService;


    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        return exerciseAppService.getExerciseAllCategories();
    }


    @GetMapping("/categories/{categoryId}/levels")
    public ResponseEntity<?> getLevels(
            @PathVariable @NotNull @Min(1) Long categoryId) {
        return exerciseAppService.getExerciseLevelsByCategoryId(categoryId);
    }


    @GetMapping("/levels/{levelId}/subcategories")
    public ResponseEntity<?> getSubCategories(
            @PathVariable @NotNull @Min(1) Long levelId) {
        return exerciseAppService.getExerciseSubCategoriesByLevelId(levelId);
    }


    @GetMapping("/subcategories/{subCategoryId}/exercises")
    public ResponseEntity<?> getExercises(
            @PathVariable @NotNull @Min(1) Long subCategoryId) {
        return exerciseAppService.getExercisesBySubCategoryId(subCategoryId);
    }


    @GetMapping("/categories/suggestions")
    public ResponseEntity<?> getAllCategoriesSuggestions(
            @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return exerciseAppService.getExerciseAllCategoriesSuggestion(query);
    }


    @GetMapping("/subcategories/suggestions/{levelId}")
    public ResponseEntity<?> getSubCategoriesSuggestions(@PathVariable @NotNull @Min(1) Long levelId,
                                                         @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return exerciseAppService.getExerciseSubCategoriesSuggestion(levelId, query);
    }


    @GetMapping("/exercises/suggestions/{subCategoryId}")
    public ResponseEntity<?> getExercisesSuggestions(@PathVariable @NotNull @Min(1) Long subCategoryId,
                                                     @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return exerciseAppService.getExercisesDetailSuggestion(subCategoryId, query);
    }
}
