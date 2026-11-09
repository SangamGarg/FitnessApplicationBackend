package com.fitnessapp.calculator.service.calculatorServiceUtilities;

import com.fitnessapp.calculator.model.dtos.requestDto.BurnedCaloriesFromActivityRequestDto;
import com.fitnessapp.calculator.model.entities.BurnedCaloriesActivityEntity;
import com.fitnessapp.calculator.repo.BurnedCaloriesFromActivityRepository;

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
        double metValue = activity.getMetValue();
        if (metValue <= 0) {
            throw new IllegalStateException("Activity MET value must be positive");
        }
        double caloriesPerHour = metValue * 3.5 * weightKg;
        double totalCalories = caloriesPerHour * (durationMin / 60.0);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name", activity.getName());
        resultMap.put("calories_per_hour", (int) caloriesPerHour);
        resultMap.put("duration_minutes", durationMin);
        resultMap.put("total_calories", (int) totalCalories);
        return resultMap;
    }

}
