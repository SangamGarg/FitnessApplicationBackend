package com.fitnessapp.fitnessCalculators.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.fitnessCalculators.shared.ClientCalculatorUtils;
import com.fitnessapp.fitnessCalculators.model.dtos.requestDto.BodyFatRequestDto;
import com.fitnessapp.fitnessCalculators.model.dtos.requestDto.BurnedCaloriesFromActivityRequestDto;
import com.fitnessapp.fitnessCalculators.model.dtos.requestDto.DailyCaloriesRequestDto;
import com.fitnessapp.fitnessCalculators.service.CalculatorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ClientCalculatorUtils.API_PREFIX)
@Validated
@Tag(name = "Calculator", description = "Endpoints for calculator")
public class CalculatorController {
    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @Operation(summary = "get Bmi", description = """
        Executes the get bmi operation.
        """)
    @GetMapping("/bmi")
    public ResponseEntity<?> getBmi(
            @RequestParam
            @Positive(message = "Weight must be greater than 0")
            @Max(value = 300, message = "Weight must be at most 300kg")
            float weightKg,

            @RequestParam
            @Positive(message = "Height must be greater than 0")
            @Max(value = 300, message = "Height must be at most 300cm")
            float heightCm) {
        return calculatorService.getBmi(weightKg, heightCm);
    }

    @Operation(summary = "get Body Fat", description = """
        Executes the get body fat operation.
        """)
    @PostMapping("/body-fat")
    public ResponseEntity<?> getBodyFat(@Valid @RequestBody BodyFatRequestDto bodyFatRequestDto) {
        return calculatorService.getBodyFat(bodyFatRequestDto);
    }

    @Operation(summary = "get Burned Calories From Activity", description = """
        Executes the get burned calories from activity operation.
        """)
    @PostMapping("/burned-calories-from-activity")
    public ResponseEntity<?> getBurnedCaloriesFromActivity(
            @Valid @RequestBody BurnedCaloriesFromActivityRequestDto requestDto) {
        return calculatorService.getBurnedCaloriesFromActivity(requestDto);
    }

    @Operation(summary = "get Burned Calories Suggestions", description = """
        Executes the get burned calories suggestions operation.
        """)
    @GetMapping("/burned-calories-from-activity/suggestions")
    public ResponseEntity<?> getBurnedCaloriesSuggestions(
            @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return calculatorService.getBurnedCaloriesSuggestion(query);
    }

    @Operation(summary = "get Calories In Food", description = """
        Executes the get calories in food operation.
        """)
    @GetMapping("/food-calories")
    public ResponseEntity<?> getCaloriesInFood(
            @RequestParam @NotBlank(message = "Query must not be blank") String query,
            @RequestParam @Positive(message = "Quantity should be greater than 0") double quantityGm
    ) {
        return calculatorService.getCaloriesInFood(query, quantityGm);
    }

    @Operation(summary = "get Calories In Food Suggestions", description = """
        Executes the get calories in food suggestions operation.
        """)
    @GetMapping("/food-calories/suggestions")
    public ResponseEntity<?> getCaloriesInFoodSuggestions(
            @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return calculatorService.getCaloriesInFoodSuggestion(query);
    }

    @Operation(summary = "get Daily Calories", description = """
        Executes the get daily calories operation.
        """)
    @PostMapping("/daily-calories")
    public ResponseEntity<?> getDailyCalories(@Valid @RequestBody DailyCaloriesRequestDto requestDto) {
        return calculatorService.getDailyCalories(requestDto);
    }

    @Operation(summary = "get Ideal Weight", description = """
        Executes the get ideal weight operation.
        """)
    @GetMapping("/ideal-weight")
    public ResponseEntity<?> getIdealWeight(
            @RequestParam
            @NotBlank(message = "Gender must not be blank")
            @Pattern(regexp = "^(?i)(male|female)$", message = "Gender must be male or female")
            String gender,

            @RequestParam
            @Positive(message = "Height must be greater than 0")
            @Max(value = 300, message = "Height must be at most 300cm")
            float heightCm) {
        return calculatorService.getIdealWeight(gender, heightCm);
    }

}
