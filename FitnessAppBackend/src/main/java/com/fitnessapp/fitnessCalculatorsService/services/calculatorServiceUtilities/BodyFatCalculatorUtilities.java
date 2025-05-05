package com.fitnessapp.fitnessCalculatorsService.services.calculatorServiceUtilities;

import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.BodyFatRequestDto;

import java.util.HashMap;
import java.util.Map;

public final class BodyFatCalculatorUtilities {
    public static Map<String, Object> createMap(BodyFatRequestDto bodyFatRequestDto) {
        // Calculate body fat using the U.S. Navy Method
        float bodyFatPercentage = calculateBodyFat(bodyFatRequestDto.getGender(), bodyFatRequestDto.getWeightKg(), bodyFatRequestDto.getHeightCm(), bodyFatRequestDto.getWaist(), bodyFatRequestDto.getHip(), bodyFatRequestDto.getNeck());

        // Calculate body fat mass
        float bodyFatMass = calculateFatBodyMass(bodyFatRequestDto.getWeightKg(), bodyFatPercentage);

        // Calculate lean body mass
        float leanBodyMass = calculateLeanBodyMass(bodyFatRequestDto.getWeightKg(), bodyFatMass);

        // Calculate body fat using the BMI method
        float bodyFatFromBMI = calculateBodyFatFromBMI(bodyFatRequestDto.getGender(), bodyFatRequestDto.getHeightCm(), bodyFatRequestDto.getWeightKg(), bodyFatRequestDto.getAge());
        Map<String, Object> data = new HashMap<>();
        data.put("BodyFatPercentage (U.S.NAVY)", bodyFatPercentage);
        data.put("BodyFatMass", bodyFatMass);
        data.put("LeanBodyMass", leanBodyMass);
        data.put("BodyFatFromBMI", bodyFatFromBMI);
        return data;
    }

    private static float calculateBodyFat(String gender, float weight, float height, float waist, float hip, float neck) {
        // Calculate body fat percentage using the U.S. Navy Method
        float bodyFatPercentage;
        if (gender.equalsIgnoreCase("male")) {
            // Formula for males
            bodyFatPercentage = (float) (495 / (1.0324 - (0.19077 * Math.log10(waist - neck)) + (0.15456 * Math.log10(height)))) - 450;

        } else {
            // Formula for females
            bodyFatPercentage = (float) (495 / (1.29579 - (0.35004 * Math.log10(waist + hip - neck)) + (0.22100 * Math.log10(height)))) - 450;
        }
        return Float.parseFloat(String.format("%.2f", bodyFatPercentage));
    }

    private static float calculateFatBodyMass(float weight, float bodyFatPercentage) {
        // Calculate body fat mass
        return Float.parseFloat(String.format("%.2f", weight * bodyFatPercentage / 100));
    }

    private static float calculateLeanBodyMass(float weight, float bodyFatMass) {
        // Calculate lean body mass
        return Float.parseFloat(String.format("%.2f", weight - bodyFatMass));
    }

    private static float calculateBodyFatFromBMI(String gender, float height, float weight, int age) {
        // Calculate body fat using the BMI method
        float h = height / 100;
        float bmi = weight / (h * h); // BMI formula: weight (kg) / height (cm) ^ 2
        if (gender.equalsIgnoreCase("male")) {
            // Formula for males
            return Float.parseFloat(String.format("%.2f", ((1.20 * bmi) + (0.23 * age) - 16.2)));

        } else {
            // Formula for females
            return Float.parseFloat(String.format("%.2f", ((1.20 * bmi) + (0.23 * age) - 5.4)));
        }
    }
}
