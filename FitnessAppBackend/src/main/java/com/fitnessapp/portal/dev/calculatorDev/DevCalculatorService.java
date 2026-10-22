package com.fitnessapp.portal.dev.calculatorDev;

import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Dev-only operations for seeding and patching calculator reference data.
 */
public interface DevCalculatorService {

    /** Bulk-inserts calories-in-food items. All fields are required; names must be unique. */
    ResponseEntity<?> uploadCalculatorCaloriesInFood(List<DevCalculatorUploadCaloriesInFoodRequestDto> requestDto);

    /** Partially updates an existing calories-in-food item, looked up by name (case-insensitive). */
    ResponseEntity<?> patchCalculatorCaloriesInFood(DevCalculatorUploadCaloriesInFoodRequestDto requestDto);

    /** Bulk-inserts burned-calories activities. MET value and image URL are required; names must be unique. */
    ResponseEntity<?> uploadCalculatorBurnedCalories(List<DevCalculatorBurnedCaloriesRequestDto> requestDto);

    /** Partially updates an existing burned-calories activity, looked up by name (case-insensitive). */
    ResponseEntity<?> patchCalculatorBurnedCalories(DevCalculatorBurnedCaloriesRequestDto requestDto);
}
