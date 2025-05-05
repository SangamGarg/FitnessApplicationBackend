package com.fitnessapp.DeveloperAndTestingService.calculatorDevService;

import com.fitnessapp.DeveloperAndTestingService.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(DevAndTestingServiceUtility.API_PREFIX_CALCULATOR)
public class DevCalculatorController {

    private final DevCalculatorService devCalculatorService;

    public DevCalculatorController(DevCalculatorService devCalculatorService) {
        this.devCalculatorService = devCalculatorService;
    }

    @PostMapping("/upload-image-about")
    public ResponseEntity<?> uploadImageAndAbout(@RequestBody @Valid DevCalculatorUploadImageAndAboutRequestDto requestDto) {
        return devCalculatorService.uploadCalculatorImageAndAbout(requestDto);
    }

    @PatchMapping("/patch-image-about")
    public ResponseEntity<?> patchImageAndAbout(@RequestBody @Valid DevCalculatorUploadImageAndAboutRequestDto requestDto) {
        return devCalculatorService.patchCalculatorImageAndAbout(requestDto);
    }


    @PostMapping("/upload-calories-in-food")
    public ResponseEntity<?> uploadCaloriesInFood(@RequestBody @Valid @NotEmpty(message = "List Cannot be empty") List<DevCalculatorUploadCaloriesInFoodRequestDto> requestDto) {
        return devCalculatorService.uploadCalculatorCaloriesInFood(requestDto);
    }

    @PatchMapping("/patch-calories-in-food")
    public ResponseEntity<?> patchCaloriesInFood(@RequestBody @Valid DevCalculatorUploadCaloriesInFoodRequestDto requestDto) {
        return devCalculatorService.patchCalculatorCaloriesInFood(requestDto);
    }
    @PostMapping("/upload-burned-calories")
    public ResponseEntity<?> uploadBurnedCalories(@RequestBody @Valid @NotEmpty(message = "List Cannot be empty") List<DevCalculatorBurnedCaloriesRequestDto> requestDto) {
        return devCalculatorService.uploadCalculatorBurnedCalories(requestDto);
    }

    @PatchMapping("/patch-burned-calories")
    public ResponseEntity<?> patchBurnedCalories(@RequestBody @Valid DevCalculatorBurnedCaloriesRequestDto requestDto) {
        return devCalculatorService.patchCalculatorBurnedCalories(requestDto);
    }
}
