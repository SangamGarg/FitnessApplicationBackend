package com.fitnessapp.exercise.portal.service;

import com.fitnessapp.exercise.portal.model.ExercisePortalCategoryRequestDto;
import com.fitnessapp.exercise.portal.model.ExercisePortalDetailsRequestDto;
import com.fitnessapp.exercise.portal.model.ExercisePortalLevelRequestDto;
import com.fitnessapp.exercise.portal.model.ExercisePortalSubCategoryRequestDto;
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