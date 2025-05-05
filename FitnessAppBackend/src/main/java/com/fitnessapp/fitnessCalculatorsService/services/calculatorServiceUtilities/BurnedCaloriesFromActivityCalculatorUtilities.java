package com.fitnessapp.fitnessCalculatorsService.services.calculatorServiceUtilities;

import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.BurnedCaloriesFromActivityRequestDto;
import com.fitnessapp.fitnessCalculatorsService.models.entities.BurnedCaloriesActivityEntity;
import com.fitnessapp.fitnessCalculatorsService.repositories.BurnedCaloriesFromActivityRepository;

import java.util.HashMap;
import java.util.Map;

public class BurnedCaloriesFromActivityCalculatorUtilities {
    public static Map<String, Object> createMap(BurnedCaloriesFromActivityRequestDto burnedCaloriesFromActivityRequestDto, BurnedCaloriesFromActivityRepository burnedCaloriesFromActivityRepository) {

        String activityName = burnedCaloriesFromActivityRequestDto.getActivityName();
        Float weightKg = burnedCaloriesFromActivityRequestDto.getWeightKg();
        Integer durationMin = burnedCaloriesFromActivityRequestDto.getDurationMin();


        BurnedCaloriesActivityEntity activity = burnedCaloriesFromActivityRepository.findByNameIgnoreCase(activityName)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        return getStringObjectMap(durationMin, activity, weightKg);
    }

    private static Map<String, Object> getStringObjectMap(Integer durationMin, BurnedCaloriesActivityEntity activity, Float weightKg) {
        double hours = durationMin / 60.0;

        double metValue = activity.getMetValue();

        double caloriesPerHour = (metValue * weightKg * 60) / 200;

        // Total calories = MET * weightKg * hours
        double totalCalories = (metValue * weightKg * durationMin) / 200;

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name", activity.getName());
        resultMap.put("calories_per_hour", (int) caloriesPerHour);
        resultMap.put("duration_minutes", durationMin);
        resultMap.put("total_calories", (int) totalCalories);
        return resultMap;
    }

}
