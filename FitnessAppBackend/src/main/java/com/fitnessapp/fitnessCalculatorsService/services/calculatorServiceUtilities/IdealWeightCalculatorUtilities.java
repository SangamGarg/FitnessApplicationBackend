package com.fitnessapp.fitnessCalculatorsService.services.calculatorServiceUtilities;

import java.util.LinkedHashMap;
import java.util.Map;

public final class IdealWeightCalculatorUtilities {

    public static Map<String, Object> createMap(String gender, float height) {

        float hamwi = IdealWeightCalculatorUtilities.calculateHamwiIdealWeight(height, gender);
        float devine = IdealWeightCalculatorUtilities.calculateDevineIdealWeight(height, gender);
        float miller = IdealWeightCalculatorUtilities.calculateMillerIdealWeight(height, gender);
        float robinson = IdealWeightCalculatorUtilities.calculateRobinsonIdealWeight(height, gender);


        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Hamwi", hamwi);
        map.put("Devine", devine);
        map.put("Miller", miller);
        map.put("Robinson", robinson);
        return map;
    }

    private static float calculateHamwiIdealWeight(float height, String gender) {
        // Calculate Hamwi ideal weight based on height and gender
        float idealWeight;
        if (gender.equalsIgnoreCase("male")) {
            idealWeight = 48f + 2.7f * ((height - 152.4f) / 2.54f);
        } else {
            idealWeight = 45.5f + 2.2f * ((height - 152.4f) / 2.54f);
        }
        return Float.parseFloat(String.format("%.2f", idealWeight));
    }

    private static float calculateDevineIdealWeight(float height, String gender) {
        // Calculate Devine ideal weight based on height and gender
        float idealWeight;
        if (gender.equalsIgnoreCase("male")) {
            idealWeight = 50f + 2.3f * ((height - 152.4f) / 2.54f);
        } else {
            idealWeight = 45.5f + 2.3f * ((height - 152.4f) / 2.54f);
        }
        return Float.parseFloat(String.format("%.2f", idealWeight));
    }

    private static float calculateMillerIdealWeight(float height, String gender) {
        // Calculate Miller ideal weight based on height and gender
        float idealWeight;
        if (gender.equalsIgnoreCase("male")) {
            idealWeight = 56.2f + 1.41f * ((height - 152.4f) / 2.54f);
        } else {
            idealWeight = 53.1f + 1.36f * ((height - 152.4f) / 2.54f);
        }
        return Float.parseFloat(String.format("%.2f", idealWeight));
    }

    private static float calculateRobinsonIdealWeight(float height, String gender) {
        // Calculate Robinson ideal weight based on height and gender
        float idealWeight;
        if (gender.equalsIgnoreCase("male")) {
            idealWeight = 52f + 1.9f * ((height - 152.4f) / 2.54f);
        } else {
            idealWeight = 49f + 1.7f * ((height - 152.4f) / 2.54f);
        }
        return Float.parseFloat(String.format("%.2f", idealWeight));
    }
}