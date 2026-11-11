package com.fitnessapp.calculator.service;

import com.fitnessapp.calculator.model.dtos.requestDto.BodyFatRequestDto;
import com.fitnessapp.calculator.model.dtos.requestDto.BurnedCaloriesFromActivityRequestDto;
import com.fitnessapp.calculator.model.dtos.requestDto.DailyCaloriesRequestDto;
import org.springframework.http.ResponseEntity;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public interface CalculatorService {

    List<Map<String, Object>> getCatalog();
    Map<String, Object> calculate(String key, Map<String, Object> in);
    Map<String, Object> getFitnessScore(ZoneId zoneId);

    ResponseEntity<?> getBmi(float weightKg, float heightCm);

    ResponseEntity<?> getBodyFat(BodyFatRequestDto bodyFatRequestDto);

    ResponseEntity<?> getBurnedCaloriesFromActivity(BurnedCaloriesFromActivityRequestDto burnedCaloriesFromActivityRequestDto);

    ResponseEntity<?> getCaloriesInFood(String query, double quantityGm);

    ResponseEntity<?> getDailyCalories(DailyCaloriesRequestDto dailyCaloriesRequestDto);

    ResponseEntity<?> getIdealWeight(String gender, float height);

    ResponseEntity<?> getCaloriesInFoodSuggestion(String query);

    ResponseEntity<?> getBurnedCaloriesSuggestion(String query);
}