package com.fitnessapp.fitnessCalculators.service;

import com.fitnessapp.fitnessCalculators.model.dtos.requestDto.BodyFatRequestDto;
import com.fitnessapp.fitnessCalculators.model.dtos.requestDto.BurnedCaloriesFromActivityRequestDto;
import com.fitnessapp.fitnessCalculators.model.dtos.requestDto.DailyCaloriesRequestDto;
import org.springframework.http.ResponseEntity;

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