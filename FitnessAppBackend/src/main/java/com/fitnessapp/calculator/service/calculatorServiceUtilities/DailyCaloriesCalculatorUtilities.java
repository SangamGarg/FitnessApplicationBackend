package com.fitnessapp.calculator.service.calculatorServiceUtilities;

import com.fitnessapp.calculator.model.dtos.requestDto.DailyCaloriesRequestDto;

import java.util.LinkedHashMap;
import java.util.Map;

public final class DailyCaloriesCalculatorUtilities {

    public static Map<String, Object> createMap(DailyCaloriesRequestDto dailyCaloriesRequestDto) {
        Float weight = dailyCaloriesRequestDto.getWeightKg();
        Float height = dailyCaloriesRequestDto.getHeightCm();
        int age = dailyCaloriesRequestDto.getAge();
        String gender = dailyCaloriesRequestDto.getGender();
        String activityLevel = dailyCaloriesRequestDto.getActivityLevel();
        int bmr = calculateBMR(weight, height, age, gender);
        int dailyCalories = calculateDailyCalories(bmr, activityLevel);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("BMR", bmr);
        map.put("BMRFormula", "Mifflin-St Jeor estimate; height in cm, weight in kg, age in years");
        map.put("activityMultiplier", activityMultiplier(activityLevel));
        map.put("goals", createGoalsMap(dailyCalories));
        map.put("note", "These calorie targets are estimates, not medical advice or a diagnosis.");
        return map;
    }

    private static int calculateBMR(float weight, float height, int age, String gender) {
        // BMR calculation based on gender
        if (gender.equalsIgnoreCase("male")) {
            return (int) ((10 * weight) + (6.25 * height) - (5 * age) + 5);
        } else {
            return (int) ((10 * weight) + (6.25 * height) - (5 * age) - 161);
        }
    }

    private static int calculateDailyCalories(int bmr, String activityLevel) {
        // Activity level multiplier based on different levels
        double multiplier = activityMultiplier(activityLevel);
        // Calculate daily calories based on BMR and activity level.
        return (int) (bmr * multiplier);
    }

    private static double activityMultiplier(String activityLevel) {
        return switch (activityLevel.toLowerCase()) {
            case "level_1" -> 1.2; // Sedentary: little or no exercise
            case "level_2" -> 1.375; // ExerciseDetailEntity 1-3 times/week
            case "level_3" -> 1.55; // ExerciseDetailEntity 4-5 times/week
            case "level_4" -> 1.725; // Daily exercise or intense exercise 3-4 times/week
            case "level_5" -> 1.9; // Intense exercise 6-7 times/week
            default -> throw new IllegalArgumentException("Unsupported activity level: " + activityLevel);
        };
    }

    private static Map<String, Object> createGoalsMap(int dailyCalories) {
        // Create a goal map with different weight goals and corresponding calorie requirements
        Map<String, Object> goalsMap = new LinkedHashMap<>();
        goalsMap.put("Maintain weight", dailyCalories);
        addWeightLossGoal(goalsMap, "Mild weight loss", "0.25 kg", dailyCalories - 250);
        addWeightLossGoal(goalsMap, "Weight loss", "0.50 kg", dailyCalories - 500);
        addWeightLossGoal(goalsMap, "Extreme weight loss", "1 kg", dailyCalories - 1000);
        goalsMap.put("Mild weight gain", createWeightGainMap("0.25 kg", dailyCalories + 250));
        goalsMap.put("Weight gain", createWeightGainMap("0.50 kg", dailyCalories + 500));
        goalsMap.put("Extreme weight gain", createWeightGainMap("1 kg", dailyCalories + 1000));
        return goalsMap;
    }

    private static void addWeightLossGoal(
            Map<String, Object> goalsMap,
            String goal,
            String weeklyChange,
            int targetCalories
    ) {
        if (targetCalories > 0) {
            goalsMap.put(goal, createWeightLossMap(weeklyChange, targetCalories));
        }
    }

    private static Map<String, Object> createWeightGainMap(String weightChange, int calorie) {
        // Create a weight goal map with weight change and corresponding calorie requirement
        Map<String, Object> weightMap = new LinkedHashMap<>();
        weightMap.put("gain_weight", weightChange);
        weightMap.put("calory", calorie);
        return weightMap;
    }

    private static Map<String, Object> createWeightLossMap(String weightChange, int calorie) {
        // Create a weight goal map with weight change and corresponding calorie requirement
        Map<String, Object> weightMap = new LinkedHashMap<>();
        weightMap.put("loss_weight", weightChange);
        weightMap.put("calory", calorie);
        return weightMap;
    }
}