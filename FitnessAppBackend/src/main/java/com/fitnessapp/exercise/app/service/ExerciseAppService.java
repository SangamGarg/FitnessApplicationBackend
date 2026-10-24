package com.fitnessapp.exercise.app.service;

import org.springframework.http.ResponseEntity;


public interface ExerciseAppService {
    ResponseEntity<?> getExerciseAllCategories();

    ResponseEntity<?> getExerciseLevelsByCategoryId(Long categoryId);

    ResponseEntity<?> getExerciseSubCategoriesByLevelId(Long levelId);

    ResponseEntity<?> getExercisesBySubCategoryId(Long subCategoryId);

    ResponseEntity<?> getExerciseSubCategoriesSuggestion(Long levelId, String query);

    ResponseEntity<?> getExerciseAllCategoriesSuggestion(String query);

    ResponseEntity<?> getExercisesDetailSuggestion(Long subCategoryId, String query);

}