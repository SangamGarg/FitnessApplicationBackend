package com.fitnessapp.exercise.app.service;

import com.fitnessapp.exercise.app.model.dto.response.ExerciseHomeResponseDto;
import org.springframework.http.ResponseEntity;


public interface ExerciseAppService {
    ResponseEntity<ExerciseHomeResponseDto> getExerciseHome();
    ResponseEntity<?> getExerciseAllCategories();

    ResponseEntity<?> getExerciseLevelsByCategoryId(Long categoryId);

    ResponseEntity<?> getExerciseSubCategoriesByLevelId(Long levelId);

    ResponseEntity<?> getExercisesBySubCategoryId(Long subCategoryId);

    ResponseEntity<?> getExerciseSubCategoriesSuggestion(Long levelId, String query);

    ResponseEntity<?> getExerciseAllCategoriesSuggestion(String query);

    ResponseEntity<?> getExercisesDetailSuggestion(Long subCategoryId, String query);

}