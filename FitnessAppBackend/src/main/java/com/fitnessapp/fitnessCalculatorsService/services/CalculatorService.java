package com.fitnessapp.fitnessCalculatorsService.services;

import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.BodyFatRequestDto;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.BurnedCaloriesFromActivityRequestDto;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.DailyCaloriesRequestDto;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

public interface CalculatorService {
    ResponseEntity<?> getBmi(float weightKg, float heightCm);

    ResponseEntity<?> getBodyFat(BodyFatRequestDto bodyFatRequestDto);

    ResponseEntity<?> getBurnedCaloriesFromActivity(BurnedCaloriesFromActivityRequestDto burnedCaloriesFromActivityRequestDto);

    ResponseEntity<?> getCaloriesInFood(String query, double quantityGm);

    ResponseEntity<?> getDailyCalories(DailyCaloriesRequestDto dailyCaloriesRequestDto);

    ResponseEntity<?> getIdealWeight(String gender, float height);

    ResponseEntity<?> getCaloriesInFoodSuggestion(String query);

    ResponseEntity<?> getBurnedCaloriesSuggestion(String query);
}