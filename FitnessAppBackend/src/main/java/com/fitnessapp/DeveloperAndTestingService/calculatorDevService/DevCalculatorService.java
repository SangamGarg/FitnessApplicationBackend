package com.fitnessapp.DeveloperAndTestingService.calculatorDevService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DevCalculatorService {
    ResponseEntity<?> uploadCalculatorImageAndAbout(DevCalculatorUploadImageAndAboutRequestDto uploadCalculatorImageAndAboutRequestDto);

    ResponseEntity<?> patchCalculatorImageAndAbout(DevCalculatorUploadImageAndAboutRequestDto uploadCalculatorImageAndAboutRequestDto);

    ResponseEntity<?> uploadCalculatorCaloriesInFood(List<DevCalculatorUploadCaloriesInFoodRequestDto> devCalculatorCaloriesInFoodRequestDto);

    ResponseEntity<?> patchCalculatorCaloriesInFood(DevCalculatorUploadCaloriesInFoodRequestDto devCalculatorCaloriesInFoodRequestDto);


    ResponseEntity<?> uploadCalculatorBurnedCalories(List<DevCalculatorBurnedCaloriesRequestDto> requestDto);

    ResponseEntity<?> patchCalculatorBurnedCalories(DevCalculatorBurnedCaloriesRequestDto requestDto);
}