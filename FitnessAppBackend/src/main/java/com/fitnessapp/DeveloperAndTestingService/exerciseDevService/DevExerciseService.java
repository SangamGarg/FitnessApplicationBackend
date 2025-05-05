package com.fitnessapp.DeveloperAndTestingService.exerciseDevService;

import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DevExerciseService {
    ResponseEntity<?> uploadCategory(DevExerciseCategoryRequestDto devExerciseCategoryRequestDto);

    ResponseEntity<?> uploadLevel(Long categoryId, DevExerciseLevelRequestDto devExerciseLevelRequestDto);

    ResponseEntity<?> uploadSubCategory(Long levelId, DevExerciseSubCategoryRequestDto devExerciseSubCategoryRequestDto);

    ResponseEntity<?> uploadExerciseDetail(Long subCategoryId, DevExerciseDetailsRequestDto devExerciseDetailsRequestDto);
}
