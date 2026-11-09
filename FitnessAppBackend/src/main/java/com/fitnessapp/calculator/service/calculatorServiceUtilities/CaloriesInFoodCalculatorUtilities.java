package com.fitnessapp.calculator.service.calculatorServiceUtilities;

import com.fitnessapp.calculator.model.entities.CaloriesInFoodEntity;
import com.fitnessapp.calculator.repo.CaloriesInFoodRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.TreeMap;

public class CaloriesInFoodCalculatorUtilities {
    public static Map<String, Object> createMap(String query, double quantityGm, CaloriesInFoodRepository caloriesInFoodRepository) {
        CaloriesInFoodEntity entity = caloriesInFoodRepository.findByNameIgnoreCase(query)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found"));
        if (entity.getServing_size_g() == null || entity.getServing_size_g() <= 0 || entity.getCalories() == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Food nutrition data is incomplete");
        }
        double multiplier = quantityGm / entity.getServing_size_g();

        Map<String, Object> response = new TreeMap<>();
        response.put("name", entity.getName());
        response.put("imageUrl", entity.getImageUrl());
        response.put("entered_quantity_g", quantityGm);
        response.put("nutrition_values_are_for", entity.getServing_size_g() + " g serving");
        response.put("calories", round(entity.getCalories() * multiplier));
        addScaledValue(response, "protein_g", entity.getProtein_g(), multiplier);
        addScaledValue(response, "carbohydrates_total_g", entity.getCarbohydrates_total_g(), multiplier);
        addScaledValue(response, "fiber_g", entity.getFiber_g(), multiplier);
        addScaledValue(response, "fat_saturated_g", entity.getFat_saturated_g(), multiplier);
        addScaledValue(response, "fat_total_g", entity.getFat_total_g(), multiplier);
        addScaledValue(response, "cholesterol_mg", entity.getCholesterol_mg(), multiplier);
        addScaledValue(response, "sugar_g", entity.getSugar_g(), multiplier);
        addScaledValue(response, "sodium_mg", entity.getSodium_mg(), multiplier);
        addScaledValue(response, "potassium_mg", entity.getPotassium_mg(), multiplier);

        return response;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static void addScaledValue(Map<String, Object> response, String name, Double value, double multiplier) {
        if (value != null) {
            response.put(name, round(value * multiplier));
        }
    }
}
