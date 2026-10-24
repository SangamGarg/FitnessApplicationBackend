package com.fitnessapp.exercise.portal;

import com.fitnessapp.exercise.portal.models.ExercisePortalCategoryRequestDto;
import com.fitnessapp.exercise.portal.models.ExercisePortalDetailsRequestDto;
import com.fitnessapp.exercise.portal.models.ExercisePortalLevelRequestDto;
import com.fitnessapp.exercise.portal.models.ExercisePortalSubCategoryRequestDto;
import org.springframework.http.ResponseEntity;

public interface ExercisePortalService {
    ResponseEntity<?> uploadCategory(ExercisePortalCategoryRequestDto devExerciseCategoryRequestDto);

    ResponseEntity<?> uploadLevel(Long categoryId, ExercisePortalLevelRequestDto exercisePortalLevelRequestDto);

    ResponseEntity<?> uploadSubCategory(Long levelId, ExercisePortalSubCategoryRequestDto exercisePortalSubCategoryRequestDto);

    ResponseEntity<?> uploadExerciseDetail(Long subCategoryId, ExercisePortalDetailsRequestDto exercisePortalDetailsRequestDto);


    ResponseEntity<?> patchCategory(ExercisePortalCategoryRequestDto devExerciseCategoryRequestDto);

    ResponseEntity<?> patchLevel(Long categoryId, ExercisePortalLevelRequestDto exercisePortalLevelRequestDto);

    ResponseEntity<?> patchSubCategory(Long levelId, ExercisePortalSubCategoryRequestDto exercisePortalSubCategoryRequestDto);

    ResponseEntity<?> patchExerciseDetail(Long subCategoryId, ExercisePortalDetailsRequestDto exercisePortalDetailsRequestDto);


    ResponseEntity<?> deleteCategory(String name);

    ResponseEntity<?> deleteLevel(Long categoryId, String level);

    ResponseEntity<?> deleteSubCategory(Long levelId, String subCategory);

    ResponseEntity<?> deleteExerciseDetail(Long subCategoryId, String exerciseName);


}