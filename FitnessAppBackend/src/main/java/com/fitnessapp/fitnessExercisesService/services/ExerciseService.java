package com.fitnessapp.fitnessExercisesService.services;

import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseCategoryEntity;
import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseDetailEntity;
import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseLevelEntity;
import com.fitnessapp.fitnessExercisesService.models.entities.ExerciseSubCategoryEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ExerciseService {
    public ResponseEntity<?> getExerciseAllCategories();

    public ResponseEntity<?> getExerciseLevelsByCategoryId(Long categoryId);

    public ResponseEntity<?> getExerciseSubCategoriesByLevelId(Long levelId);

    public ResponseEntity<?> getExercisesBySubCategoryId(Long subCategoryId);

    ResponseEntity<?> getExerciseSubCategoriesSuggestion(Long levelId, String query);

    ResponseEntity<?> getExerciseAllCategoriesSuggestion(String query);

    ResponseEntity<?> getExercisesDetailSuggestion(Long subCategoryId, String query);

}