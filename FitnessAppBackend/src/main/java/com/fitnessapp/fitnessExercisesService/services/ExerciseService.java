package com.fitnessapp.fitnessExercisesService.services;

import org.springframework.http.ResponseEntity;


public interface ExerciseService {
    ResponseEntity<?> getExerciseAllCategories();

    ResponseEntity<?> getExerciseLevelsByCategoryId(Long categoryId);

    ResponseEntity<?> getExerciseSubCategoriesByLevelId(Long levelId);

    ResponseEntity<?> getExercisesBySubCategoryId(Long subCategoryId);

    ResponseEntity<?> getExerciseSubCategoriesSuggestion(Long levelId, String query);

    ResponseEntity<?> getExerciseAllCategoriesSuggestion(String query);

    ResponseEntity<?> getExercisesDetailSuggestion(Long subCategoryId, String query);

}