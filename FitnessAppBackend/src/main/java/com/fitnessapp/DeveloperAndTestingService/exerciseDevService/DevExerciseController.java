package com.fitnessapp.DeveloperAndTestingService.exerciseDevService;

import com.fitnessapp.DeveloperAndTestingService.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
