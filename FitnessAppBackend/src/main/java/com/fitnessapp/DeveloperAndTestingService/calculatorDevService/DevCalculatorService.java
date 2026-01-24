package com.fitnessapp.DeveloperAndTestingService.calculatorDevService;

import com.fitnessapp.imageAndAboutService.models.UploadImageAndAboutRequestDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DevCalculatorService {


    ResponseEntity<?> uploadCalculatorCaloriesInFood(List<DevCalculatorUploadCaloriesInFoodRequestDto> devCalculatorCaloriesInFoodRequestDto);

    ResponseEntity<?> patchCalculatorCaloriesInFood(DevCalculatorUploadCaloriesInFoodRequestDto devCalculatorCaloriesInFoodRequestDto);


    ResponseEntity<?> uploadCalculatorBurnedCalories(List<DevCalculatorBurnedCaloriesRequestDto> requestDto);

    ResponseEntity<?> patchCalculatorBurnedCalories(DevCalculatorBurnedCaloriesRequestDto requestDto);
}