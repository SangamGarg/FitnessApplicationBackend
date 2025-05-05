package com.fitnessapp.fitnessCalculatorsService.services.calculatorServiceUtilities;

import com.fitnessapp.fitnessCalculatorsService.models.entities.CaloriesInFoodEntity;
import com.fitnessapp.fitnessCalculatorsService.repositories.CaloriesInFoodRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CaloriesInFoodCalculatorUtilities {
    public static Map<String, Object> createMap(String query, double quantityGm, CaloriesInFoodRepository caloriesInFoodRepository) {
        CaloriesInFoodEntity entity = caloriesInFoodRepository.findByNameIgnoreCase(query)
                .orElseThrow(() -> new RuntimeException("Food item not found"));
        double multiplier = quantityGm / entity.getServing_size_g();

        Map<String, Object> response = new TreeMap<>();
        response.put("name", entity.getName());
        response.put("imageUrl", entity.getImageUrl());
        response.put("entered_quantity_g", quantityGm);
        response.put("calories", round(entity.getCalories() * multiplier));
        response.put("protein_g", round(entity.getProtein_g() * multiplier));
        response.put("carbohydrates_total_g", round(entity.getCarbohydrates_total_g() * multiplier));
        response.put("fiber_g", round(entity.getFiber_g() * multiplier));
        response.put("fat_saturated_g", round(entity.getFat_saturated_g() * multiplier));
        response.put("fat_total_g", round(entity.getFat_total_g() * multiplier));
        response.put("cholesterol_mg", round(entity.getCholesterol_mg() * multiplier));
        response.put("sugar_g", round(entity.getSugar_g() * multiplier));
        response.put("sodium_mg", round(entity.getSodium_mg() * multiplier));
        response.put("potassium_mg", round(entity.getPotassium_mg() * multiplier));

        return response;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
