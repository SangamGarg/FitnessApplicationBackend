package com.fitnessapp.calculator.controller;

import com.fitnessapp.calculator.model.SmartCalculationRequest;
import com.fitnessapp.calculator.model.dtos.requestDto.BodyFatRequestDto;
import com.fitnessapp.calculator.model.dtos.requestDto.BurnedCaloriesFromActivityRequestDto;
import com.fitnessapp.calculator.model.dtos.requestDto.DailyCaloriesRequestDto;
import com.fitnessapp.calculator.service.CalculatorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Map;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_CALCULATOR_ENDPOINT;

@RestController
@RequestMapping(APP_CALCULATOR_ENDPOINT)
@Validated
@RequiredArgsConstructor
@Slf4j
public class AppCalculatorController {
    private final CalculatorService calculatorService;

    @GetMapping("/catalog")
    public ResponseEntity<?> getCatalog() {
        log.info("Fetching smart calculator catalog");
        var catalog = calculatorService.getCatalog();
        log.debug("Smart calculator catalog fetched successfully");
        return ResponseEntity.ok(catalog);
    }

    @PostMapping("/{calculatorKey}")
    public ResponseEntity<Map<String, Object>> calculate(
            @PathVariable String calculatorKey,
            @Valid @RequestBody SmartCalculationRequest request) {
        log.info("Executing smart calculation for calculatorKey='{}' with input keys={}",
                calculatorKey, request.inputs() != null ? request.inputs().keySet() : "null");
        log.debug("Smart calculation payload for calculatorKey='{}': {}", calculatorKey, request);

        Map<String, Object> result = calculatorService.calculate(calculatorKey, request.inputs());

        log.info("Completed smart calculation for calculatorKey='{}'", calculatorKey);
        log.debug("Calculation result for calculatorKey='{}': {}", calculatorKey, result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/fitness-score")
    public ResponseEntity<Map<String, Object>> getFitnessScore(
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        log.info("Fetching fitness score for timeZone='{}'", timeZone);
        try {
            ZoneId zoneId = ZoneId.of(timeZone);
            Map<String, Object> fitnessScore = calculatorService.getFitnessScore(zoneId);
            log.info("Successfully calculated fitness score for zoneId='{}'", zoneId);
            return ResponseEntity.ok(fitnessScore);
        } catch (DateTimeException ex) {
            log.warn("Invalid timeZone provided: '{}'. Error: {}", timeZone, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timeZone. Use an IANA zone such as Asia/Kolkata.", ex);
        }
    }

    @GetMapping("/bmi")
    public ResponseEntity<?> getBmi(
            @RequestParam @Positive(message = "Weight must be greater than 0") @Max(value = 300, message = "Weight must be at most 300kg")
            float weightKg,
            @RequestParam @Positive(message = "Height must be greater than 0") @Max(value = 300, message = "Height must be at most 300cm")
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
            @RequestParam @DecimalMin(value = "0.01", message = "Quantity should be greater than 0") @DecimalMax(value = "100000.0", message = "Quantity must not exceed 100000 grams")
            double quantityGm
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
            @RequestParam @NotBlank(message = "Gender must not be blank") @Pattern(regexp = "^(?i)(male|female)$", message = "Gender must be male or female")
            String gender,

            @RequestParam @DecimalMin(value = "120.0", message = "Height must be at least 120cm for these adult formulas") @Max(value = 300, message = "Height must be at most 300cm")
            float heightCm) {
        return calculatorService.getIdealWeight(gender, heightCm);
    }

}
