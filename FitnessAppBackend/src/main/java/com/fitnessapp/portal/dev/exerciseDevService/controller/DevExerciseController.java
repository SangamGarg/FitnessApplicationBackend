package com.fitnessapp.portal.dev.exerciseDevService.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.portal.dev.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import com.fitnessapp.portal.dev.exerciseDevService.models.DevExerciseCategoryRequestDto;
import com.fitnessapp.portal.dev.exerciseDevService.models.DevExerciseDetailsRequestDto;
import com.fitnessapp.portal.dev.exerciseDevService.models.DevExerciseLevelRequestDto;
import com.fitnessapp.portal.dev.exerciseDevService.models.DevExerciseSubCategoryRequestDto;
import com.fitnessapp.portal.dev.exerciseDevService.service.DevExerciseService;
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
@Tag(name = "Dev Exercise", description = "Endpoints for dev exercise")
public class DevExerciseController {

    private final DevExerciseService devExerciseService;

    public DevExerciseController(DevExerciseService devExerciseService) {
        this.devExerciseService = devExerciseService;
    }

    @Operation(summary = "upload Category", description = """
        Executes the upload category operation.
        """)
    @PostMapping("/upload/category")
    public ResponseEntity<?> uploadCategory(@RequestBody @Valid DevExerciseCategoryRequestDto devExerciseCategoryRequestDto) {
        return devExerciseService.uploadCategory(devExerciseCategoryRequestDto);
    }

    @Operation(summary = "upload Level", description = """
        Executes the upload level operation.
        """)
    @PostMapping("/upload/level/{categoryId}")
    public ResponseEntity<?> uploadLevel(@PathVariable @NotNull(message = "Category Id cannot be null")
                                         @Min(value = 1, message = "Minimum value is 1")
                                         Long categoryId, @RequestBody @Valid DevExerciseLevelRequestDto devExerciseLevelRequestDto) {
        return devExerciseService.uploadLevel(categoryId, devExerciseLevelRequestDto);
    }

    @Operation(summary = "upload Sub Category", description = """
        Executes the upload sub category operation.
        """)
    @PostMapping("/upload/subcategory/{levelId}")
    public ResponseEntity<?> uploadSubCategory(@PathVariable @NotNull(message = "Category Id cannot be null")
                                               @Min(value = 1, message = "Minimum value is 1") Long levelId, @RequestBody @Valid DevExerciseSubCategoryRequestDto devExerciseSubCategoryRequestDto) {
        return devExerciseService.uploadSubCategory(levelId, devExerciseSubCategoryRequestDto);
    }

    @Operation(summary = "upload Exercise", description = """
        Executes the upload exercise operation.
        """)
    @PostMapping("/upload/exercise/{subCategoryId}")
    public ResponseEntity<?> uploadExercise(@PathVariable @NotNull(message = "Category Id cannot be null")
                                            @Min(value = 1, message = "Minimum value is 1") Long subCategoryId, @RequestBody @Valid DevExerciseDetailsRequestDto devExerciseDetailsRequestDto) {
        return devExerciseService.uploadExerciseDetail(subCategoryId, devExerciseDetailsRequestDto);
    }

    @Operation(summary = "patch Category", description = """
        Executes the patch category operation.
        """)
    @PatchMapping("/patch/category")
    public ResponseEntity<?> patchCategory(@RequestBody @Valid DevExerciseCategoryRequestDto devExerciseCategoryRequestDto) {
        return devExerciseService.patchCategory(devExerciseCategoryRequestDto);
    }

    @Operation(summary = "patch Level", description = """
        Executes the patch level operation.
        """)
    @PatchMapping("/patch/level/{categoryId}")
    public ResponseEntity<?> patchLevel(@PathVariable @NotNull(message = "Category Id cannot be null")
                                        @Min(value = 1, message = "Minimum value is 1")
                                        Long categoryId, @RequestBody @Valid DevExerciseLevelRequestDto devExerciseLevelRequestDto) {
        return devExerciseService.patchLevel(categoryId, devExerciseLevelRequestDto);
    }

    @Operation(summary = "patch Sub Category", description = """
        Executes the patch sub category operation.
        """)
    @PatchMapping("/patch/subcategory/{levelId}")
    public ResponseEntity<?> patchSubCategory(@PathVariable @NotNull(message = "Category Id cannot be null")
                                              @Min(value = 1, message = "Minimum value is 1") Long levelId, @RequestBody @Valid DevExerciseSubCategoryRequestDto devExerciseSubCategoryRequestDto) {
        return devExerciseService.patchSubCategory(levelId, devExerciseSubCategoryRequestDto);
    }

    @Operation(summary = "patch Exercise", description = """
        Executes the patch exercise operation.
        """)
    @PatchMapping("/patch/exercise/{subCategoryId}")
    public ResponseEntity<?> patchExercise(@PathVariable @NotNull(message = "Category Id cannot be null")
                                           @Min(value = 1, message = "Minimum value is 1") Long subCategoryId, @RequestBody @Valid DevExerciseDetailsRequestDto devExerciseDetailsRequestDto) {
        return devExerciseService.patchExerciseDetail(subCategoryId, devExerciseDetailsRequestDto);
    }


    @Operation(summary = "delete Category", description = """
        Executes the delete category operation.
        """)
    @DeleteMapping("/delete/category")
    public ResponseEntity<?> deleteCategory(@RequestParam @NotBlank(message = "Name param cannot be null or blank") String name) {
        return devExerciseService.deleteCategory(name);
    }

    @Operation(summary = "delete Level", description = """
        Executes the delete level operation.
        """)
    @DeleteMapping("/delete/level/{categoryId}")
    public ResponseEntity<?> deleteLevel(@PathVariable @NotNull(message = "Category Id cannot be null")
                                         @Min(value = 1, message = "Minimum value is 1")
                                         Long categoryId, @RequestParam @NotBlank(message = "Level cannot be null or blank") String level) {
        return devExerciseService.deleteLevel(categoryId, level);
    }

    @Operation(summary = "delete Sub Category", description = """
        Executes the delete sub category operation.
        """)
    @DeleteMapping("/delete/subcategory/{levelId}")
    public ResponseEntity<?> deleteSubCategory(@PathVariable @NotNull(message = "Category Id cannot be null")
                                               @Min(value = 1, message = "Minimum value is 1") Long levelId, @RequestParam @NotBlank(message = "Sub Category cannot be null or blank") String subCategory) {
        return devExerciseService.deleteSubCategory(levelId, subCategory);
    }

    @Operation(summary = "delete Exercise", description = """
        Executes the delete exercise operation.
        """)
    @DeleteMapping("/delete/exercise/{subCategoryId}")
    public ResponseEntity<?> deleteExercise(@PathVariable @NotNull(message = "Category Id cannot be null")
                                            @Min(value = 1, message = "Minimum value is 1") Long subCategoryId, @RequestParam @NotBlank(message = "Exercise Name cannot be null or blank") String exerciseName) {
        return devExerciseService.deleteExerciseDetail(subCategoryId, exerciseName);
    }


}
