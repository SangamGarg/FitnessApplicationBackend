package com.fitnessapp.exercise.portal;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.portal.dev.shared.DevUtils;
import com.fitnessapp.exercise.portal.models.ExercisePortalCategoryRequestDto;
import com.fitnessapp.exercise.portal.models.ExercisePortalDetailsRequestDto;
import com.fitnessapp.exercise.portal.models.ExercisePortalLevelRequestDto;
import com.fitnessapp.exercise.portal.models.ExercisePortalSubCategoryRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(DevUtils.API_PREFIX_EXERCISE)
@Validated
@Tag(name = "Dev Exercise", description = "Endpoints for dev exercise")
public class ExercisePortalController {

    private final ExercisePortalService exercisePortalService;

    @Operation(summary = "upload Category", description = """
        Executes the upload category operation.
        """)
    @PostMapping("/upload/category")
    public ResponseEntity<?> uploadCategory(@RequestBody @Valid ExercisePortalCategoryRequestDto devExerciseCategoryRequestDto) {
        log.debug("Received request to upload exercise category");
        return exercisePortalService.uploadCategory(devExerciseCategoryRequestDto);
    }

    @Operation(summary = "upload Level", description = """
        Executes the upload level operation.
        """)
    @PostMapping("/upload/level/{categoryId}")
    public ResponseEntity<?> uploadLevel(@PathVariable @NotNull(message = "Category Id cannot be null")
                                         @Min(value = 1, message = "Minimum value is 1")
                                         Long categoryId, @RequestBody @Valid ExercisePortalLevelRequestDto exercisePortalLevelRequestDto) {
        log.debug("Received request to upload exercise level categoryId={}", categoryId);
        return exercisePortalService.uploadLevel(categoryId, exercisePortalLevelRequestDto);
    }

    @Operation(summary = "upload Sub Category", description = """
        Executes the upload sub category operation.
        """)
    @PostMapping("/upload/subcategory/{levelId}")
    public ResponseEntity<?> uploadSubCategory(@PathVariable @NotNull(message = "Category Id cannot be null")
                                               @Min(value = 1, message = "Minimum value is 1") Long levelId, @RequestBody @Valid ExercisePortalSubCategoryRequestDto exercisePortalSubCategoryRequestDto) {
        log.debug("Received request to upload exercise subcategory levelId={}", levelId);
        return exercisePortalService.uploadSubCategory(levelId, exercisePortalSubCategoryRequestDto);
    }

    @Operation(summary = "upload Exercise", description = """
        Executes the upload exercise operation.
        """)
    @PostMapping("/upload/exercise/{subCategoryId}")
    public ResponseEntity<?> uploadExercise(@PathVariable @NotNull(message = "Category Id cannot be null")
                                            @Min(value = 1, message = "Minimum value is 1") Long subCategoryId, @RequestBody @Valid ExercisePortalDetailsRequestDto exercisePortalDetailsRequestDto) {
        log.debug("Received request to upload exercise detail subCategoryId={}", subCategoryId);
        return exercisePortalService.uploadExerciseDetail(subCategoryId, exercisePortalDetailsRequestDto);
    }

    @Operation(summary = "patch Category", description = """
        Executes the patch category operation.
        """)
    @PatchMapping("/patch/category")
    public ResponseEntity<?> patchCategory(@RequestBody ExercisePortalCategoryRequestDto devExerciseCategoryRequestDto) {
        log.debug("Received request to patch exercise category");
        return exercisePortalService.patchCategory(devExerciseCategoryRequestDto);
    }

    @Operation(summary = "patch Level", description = """
        Executes the patch level operation.
        """)
    @PatchMapping("/patch/level/{categoryId}")
    public ResponseEntity<?> patchLevel(@PathVariable @NotNull(message = "Category Id cannot be null")
                                        @Min(value = 1, message = "Minimum value is 1")
                                        Long categoryId, @RequestBody ExercisePortalLevelRequestDto exercisePortalLevelRequestDto) {
        log.debug("Received request to patch exercise level categoryId={}", categoryId);
        return exercisePortalService.patchLevel(categoryId, exercisePortalLevelRequestDto);
    }

    @Operation(summary = "patch Sub Category", description = """
        Executes the patch sub category operation.
        """)
    @PatchMapping("/patch/subcategory/{levelId}")
    public ResponseEntity<?> patchSubCategory(@PathVariable @NotNull(message = "Category Id cannot be null")
                                              @Min(value = 1, message = "Minimum value is 1") Long levelId, @RequestBody ExercisePortalSubCategoryRequestDto exercisePortalSubCategoryRequestDto) {
        log.debug("Received request to patch exercise subcategory levelId={}", levelId);
        return exercisePortalService.patchSubCategory(levelId, exercisePortalSubCategoryRequestDto);
    }

    @Operation(summary = "patch Exercise", description = """
        Executes the patch exercise operation.
        """)
    @PatchMapping("/patch/exercise/{subCategoryId}")
    public ResponseEntity<?> patchExercise(@PathVariable @NotNull(message = "Category Id cannot be null")
                                           @Min(value = 1, message = "Minimum value is 1") Long subCategoryId, @RequestBody ExercisePortalDetailsRequestDto exercisePortalDetailsRequestDto) {
        log.debug("Received request to patch exercise detail subCategoryId={}", subCategoryId);
        return exercisePortalService.patchExerciseDetail(subCategoryId, exercisePortalDetailsRequestDto);
    }


    @Operation(summary = "delete Category", description = """
        Executes the delete category operation.
        """)
    @DeleteMapping("/delete/category")
    public ResponseEntity<?> deleteCategory(@RequestParam @NotBlank(message = "Name param cannot be null or blank") String name) {
        return exercisePortalService.deleteCategory(name);
    }

    @Operation(summary = "delete Level", description = """
        Executes the delete level operation.
        """)
    @DeleteMapping("/delete/level/{categoryId}")
    public ResponseEntity<?> deleteLevel(@PathVariable @NotNull(message = "Category Id cannot be null")
                                         @Min(value = 1, message = "Minimum value is 1")
                                         Long categoryId, @RequestParam @NotBlank(message = "Level cannot be null or blank") String level) {
        return exercisePortalService.deleteLevel(categoryId, level);
    }

    @Operation(summary = "delete Sub Category", description = """
        Executes the delete sub category operation.
        """)
    @DeleteMapping("/delete/subcategory/{levelId}")
    public ResponseEntity<?> deleteSubCategory(@PathVariable @NotNull(message = "Category Id cannot be null")
                                               @Min(value = 1, message = "Minimum value is 1") Long levelId, @RequestParam @NotBlank(message = "Sub Category cannot be null or blank") String subCategory) {
        return exercisePortalService.deleteSubCategory(levelId, subCategory);
    }

    @Operation(summary = "delete Exercise", description = """
        Executes the delete exercise operation.
        """)
    @DeleteMapping("/delete/exercise/{subCategoryId}")
    public ResponseEntity<?> deleteExercise(@PathVariable @NotNull(message = "Category Id cannot be null")
                                            @Min(value = 1, message = "Minimum value is 1") Long subCategoryId, @RequestParam @NotBlank(message = "Exercise Name cannot be null or blank") String exerciseName) {
        return exercisePortalService.deleteExerciseDetail(subCategoryId, exerciseName);
    }


}
