package com.fitnessapp.DeveloperAndTestingService.exerciseDevService.controller;

import com.fitnessapp.DeveloperAndTestingService.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import com.fitnessapp.DeveloperAndTestingService.exerciseDevService.models.DevExerciseCategoryRequestDto;
import com.fitnessapp.DeveloperAndTestingService.exerciseDevService.models.DevExerciseDetailsRequestDto;
import com.fitnessapp.DeveloperAndTestingService.exerciseDevService.models.DevExerciseLevelRequestDto;
import com.fitnessapp.DeveloperAndTestingService.exerciseDevService.models.DevExerciseSubCategoryRequestDto;
import com.fitnessapp.DeveloperAndTestingService.exerciseDevService.service.DevExerciseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(DevAndTestingServiceUtility.API_PREFIX_EXERCISE)
@Validated
public class DevExerciseController {

    private final DevExerciseService devExerciseService;

    public DevExerciseController(DevExerciseService devExerciseService) {
        this.devExerciseService = devExerciseService;
    }

    @PostMapping("/upload/category")
    public ResponseEntity<?> uploadCategory(@RequestBody @Valid DevExerciseCategoryRequestDto devExerciseCategoryRequestDto) {
        return devExerciseService.uploadCategory(devExerciseCategoryRequestDto);
    }

    @PostMapping("/upload/level/{categoryId}")
    public ResponseEntity<?> uploadLevel(@PathVariable @NotNull(message = "Category Id cannot be null")
                                         @Min(value = 1, message = "Minimum value is 1")
                                         Long categoryId, @RequestBody @Valid DevExerciseLevelRequestDto devExerciseLevelRequestDto) {
        return devExerciseService.uploadLevel(categoryId, devExerciseLevelRequestDto);
    }

    @PostMapping("/upload/subcategory/{levelId}")
    public ResponseEntity<?> uploadSubCategory(@PathVariable @NotNull(message = "Category Id cannot be null")
                                               @Min(value = 1, message = "Minimum value is 1") Long levelId, @RequestBody @Valid DevExerciseSubCategoryRequestDto devExerciseSubCategoryRequestDto) {
        return devExerciseService.uploadSubCategory(levelId, devExerciseSubCategoryRequestDto);
    }

    @PostMapping("/upload/exercise/{subCategoryId}")
    public ResponseEntity<?> uploadExercise(@PathVariable @NotNull(message = "Category Id cannot be null")
                                            @Min(value = 1, message = "Minimum value is 1") Long subCategoryId, @RequestBody @Valid DevExerciseDetailsRequestDto devExerciseDetailsRequestDto) {
        return devExerciseService.uploadExerciseDetail(subCategoryId, devExerciseDetailsRequestDto);
    }

    @PatchMapping("/patch/category")
    public ResponseEntity<?> patchCategory(@RequestBody @Valid DevExerciseCategoryRequestDto devExerciseCategoryRequestDto) {
        return devExerciseService.patchCategory(devExerciseCategoryRequestDto);
    }

    @PatchMapping("/patch/level/{categoryId}")
    public ResponseEntity<?> patchLevel(@PathVariable @NotNull(message = "Category Id cannot be null")
                                        @Min(value = 1, message = "Minimum value is 1")
                                        Long categoryId, @RequestBody @Valid DevExerciseLevelRequestDto devExerciseLevelRequestDto) {
        return devExerciseService.patchLevel(categoryId, devExerciseLevelRequestDto);
    }

    @PatchMapping("/patch/subcategory/{levelId}")
    public ResponseEntity<?> patchSubCategory(@PathVariable @NotNull(message = "Category Id cannot be null")
                                              @Min(value = 1, message = "Minimum value is 1") Long levelId, @RequestBody @Valid DevExerciseSubCategoryRequestDto devExerciseSubCategoryRequestDto) {
        return devExerciseService.patchSubCategory(levelId, devExerciseSubCategoryRequestDto);
    }

    @PatchMapping("/patch/exercise/{subCategoryId}")
    public ResponseEntity<?> patchExercise(@PathVariable @NotNull(message = "Category Id cannot be null")
                                           @Min(value = 1, message = "Minimum value is 1") Long subCategoryId, @RequestBody @Valid DevExerciseDetailsRequestDto devExerciseDetailsRequestDto) {
        return devExerciseService.patchExerciseDetail(subCategoryId, devExerciseDetailsRequestDto);
    }


    @DeleteMapping("/delete/category")
    public ResponseEntity<?> deleteCategory(@RequestParam @NotBlank(message = "Name param cannot be null or blank") String name) {
        return devExerciseService.deleteCategory(name);
    }

    @DeleteMapping("/delete/level/{categoryId}")
    public ResponseEntity<?> deleteLevel(@PathVariable @NotNull(message = "Category Id cannot be null")
                                         @Min(value = 1, message = "Minimum value is 1")
                                         Long categoryId, @RequestParam @NotBlank(message = "Level cannot be null or blank") String level) {
        return devExerciseService.deleteLevel(categoryId, level);
    }

    @DeleteMapping("/delete/subcategory/{levelId}")
    public ResponseEntity<?> deleteSubCategory(@PathVariable @NotNull(message = "Category Id cannot be null")
                                               @Min(value = 1, message = "Minimum value is 1") Long levelId, @RequestParam @NotBlank(message = "Sub Category cannot be null or blank") String subCategory) {
        return devExerciseService.deleteSubCategory(levelId, subCategory);
    }

    @DeleteMapping("/delete/exercise/{subCategoryId}")
    public ResponseEntity<?> deleteExercise(@PathVariable @NotNull(message = "Category Id cannot be null")
                                            @Min(value = 1, message = "Minimum value is 1") Long subCategoryId, @RequestParam @NotBlank(message = "Exercise Name cannot be null or blank") String exerciseName) {
        return devExerciseService.deleteExerciseDetail(subCategoryId, exerciseName);
    }


}
