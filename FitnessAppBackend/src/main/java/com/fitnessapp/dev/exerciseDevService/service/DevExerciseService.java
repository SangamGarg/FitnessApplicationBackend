package com.fitnessapp.dev.exerciseDevService.service;

import com.fitnessapp.dev.exerciseDevService.models.DevExerciseCategoryRequestDto;
import com.fitnessapp.dev.exerciseDevService.models.DevExerciseDetailsRequestDto;
import com.fitnessapp.dev.exerciseDevService.models.DevExerciseLevelRequestDto;
import com.fitnessapp.dev.exerciseDevService.models.DevExerciseSubCategoryRequestDto;
import org.springframework.http.ResponseEntity;

public interface DevExerciseService {
    ResponseEntity<?> uploadCategory(DevExerciseCategoryRequestDto devExerciseCategoryRequestDto);

    ResponseEntity<?> uploadLevel(Long categoryId, DevExerciseLevelRequestDto devExerciseLevelRequestDto);

    ResponseEntity<?> uploadSubCategory(Long levelId, DevExerciseSubCategoryRequestDto devExerciseSubCategoryRequestDto);

    ResponseEntity<?> uploadExerciseDetail(Long subCategoryId, DevExerciseDetailsRequestDto devExerciseDetailsRequestDto);


    ResponseEntity<?> patchCategory(DevExerciseCategoryRequestDto devExerciseCategoryRequestDto);

    ResponseEntity<?> patchLevel(Long categoryId, DevExerciseLevelRequestDto devExerciseLevelRequestDto);

    ResponseEntity<?> patchSubCategory(Long levelId, DevExerciseSubCategoryRequestDto devExerciseSubCategoryRequestDto);

    ResponseEntity<?> patchExerciseDetail(Long subCategoryId, DevExerciseDetailsRequestDto devExerciseDetailsRequestDto);


    ResponseEntity<?> deleteCategory(String name);

    ResponseEntity<?> deleteLevel(Long categoryId, String level);

    ResponseEntity<?> deleteSubCategory(Long levelId, String subCategory);

    ResponseEntity<?> deleteExerciseDetail(Long subCategoryId, String exerciseName);


}