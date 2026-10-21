package com.fitnessapp.portal.dev.calculatorDevService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.portal.dev.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(DevAndTestingServiceUtility.API_PREFIX_CALCULATOR)
@Tag(name = "Dev Calculator", description = "Endpoints for dev calculator")
public class DevCalculatorController {

    private final DevCalculatorService devCalculatorService;

    public DevCalculatorController(DevCalculatorService devCalculatorService) {
        this.devCalculatorService = devCalculatorService;
    }

    @Operation(summary = "upload Calories In Food", description = """
        Executes the upload calories in food operation.
        """)
    @PostMapping("/upload-calories-in-food")
    public ResponseEntity<?> uploadCaloriesInFood(@RequestBody @Valid @NotEmpty(message = "List Cannot be empty") List<DevCalculatorUploadCaloriesInFoodRequestDto> requestDto) {
        return devCalculatorService.uploadCalculatorCaloriesInFood(requestDto);
    }

    @Operation(summary = "patch Calories In Food", description = """
        Executes the patch calories in food operation.
        """)
    @PatchMapping("/patch-calories-in-food")
    public ResponseEntity<?> patchCaloriesInFood(@RequestBody @Valid DevCalculatorUploadCaloriesInFoodRequestDto requestDto) {
        return devCalculatorService.patchCalculatorCaloriesInFood(requestDto);
    }
    @Operation(summary = "upload Burned Calories", description = """
        Executes the upload burned calories operation.
        """)
    @PostMapping("/upload-burned-calories")
    public ResponseEntity<?> uploadBurnedCalories(@RequestBody @Valid @NotEmpty(message = "List Cannot be empty") List<DevCalculatorBurnedCaloriesRequestDto> requestDto) {
        return devCalculatorService.uploadCalculatorBurnedCalories(requestDto);
    }

    @Operation(summary = "patch Burned Calories", description = """
        Executes the patch burned calories operation.
        """)
    @PatchMapping("/patch-burned-calories")
    public ResponseEntity<?> patchBurnedCalories(@RequestBody @Valid DevCalculatorBurnedCaloriesRequestDto requestDto) {
        return devCalculatorService.patchCalculatorBurnedCalories(requestDto);
    }
}
