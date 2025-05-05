package com.fitnessapp.fitnessCalculatorsService.controllers;

import com.fitnessapp.fitnessCalculatorsService.calculatorServiceUtilities.AppConstantsCalculatorService;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.BodyFatRequestDto;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.BurnedCaloriesFromActivityRequestDto;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.DailyCaloriesRequestDto;
import com.fitnessapp.fitnessCalculatorsService.services.CalculatorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstantsCalculatorService.API_PREFIX)
@Validated
public class CalculatorController {
    private CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

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

    @PostMapping("/body-fat")
    public ResponseEntity<?> getBodyFat(@Valid @RequestBody BodyFatRequestDto bodyFatRequestDto) {
        return calculatorService.getBodyFat(bodyFatRequestDto);
    }

    @PostMapping("/burned-calories-from-activity")
    public ResponseEntity<?> getBurnedCaloriesFromActivity(
            @Valid @RequestBody BurnedCaloriesFromActivityRequestDto requestDto) {
        return calculatorService.getBurnedCaloriesFromActivity(requestDto);
    }

    @GetMapping("/burned-calories-from-activity/suggestions")
    public ResponseEntity<?> getBurnedCaloriesSuggestions(
            @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return calculatorService.getBurnedCaloriesSuggestion(query);
    }

    @GetMapping("/food-calories")
    public ResponseEntity<?> getCaloriesInFood(
            @RequestParam @NotBlank(message = "Query must not be blank") String query,
            @RequestParam @Positive(message = "Quantity should be greater than 0") double quantityGm
    ) {
        return calculatorService.getCaloriesInFood(query, quantityGm);
    }

    @GetMapping("/food-calories/suggestions")
    public ResponseEntity<?> getCaloriesInFoodSuggestions(
            @RequestParam @NotBlank(message = "Query must not be blank") String query
    ) {
        return calculatorService.getCaloriesInFoodSuggestion(query);
    }

    @PostMapping("/daily-calories")
    public ResponseEntity<?> getDailyCalories(@Valid @RequestBody DailyCaloriesRequestDto requestDto) {
        return calculatorService.getDailyCalories(requestDto);
    }

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
