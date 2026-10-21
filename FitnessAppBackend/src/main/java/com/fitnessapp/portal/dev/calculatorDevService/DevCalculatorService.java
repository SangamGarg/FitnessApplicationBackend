package com.fitnessapp.portal.dev.calculatorDevService;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DevCalculatorService {


    ResponseEntity<?> uploadCalculatorCaloriesInFood(List<DevCalculatorUploadCaloriesInFoodRequestDto> devCalculatorCaloriesInFoodRequestDto);

    ResponseEntity<?> patchCalculatorCaloriesInFood(DevCalculatorUploadCaloriesInFoodRequestDto devCalculatorCaloriesInFoodRequestDto);


    ResponseEntity<?> uploadCalculatorBurnedCalories(List<DevCalculatorBurnedCaloriesRequestDto> requestDto);

    ResponseEntity<?> patchCalculatorBurnedCalories(DevCalculatorBurnedCaloriesRequestDto requestDto);
}