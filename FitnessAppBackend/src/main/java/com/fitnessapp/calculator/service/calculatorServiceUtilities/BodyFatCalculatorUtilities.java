package com.fitnessapp.calculator.service.calculatorServiceUtilities;

import com.fitnessapp.calculator.model.dtos.requestDto.BodyFatRequestDto;

import java.util.HashMap;
import java.util.Map;

public final class BodyFatCalculatorUtilities {
    public static Map<String, Object> createMap(BodyFatRequestDto bodyFatRequestDto) {
        // Calculate body fat using the U.S. Navy Method
        float bodyFatPercentage = calculateBodyFat(bodyFatRequestDto.getGender(), bodyFatRequestDto.getWeightKg(), bodyFatRequestDto.getHeightCm(), bodyFatRequestDto.getWaistCm(), bodyFatRequestDto.getHipCm(), bodyFatRequestDto.getNeckCm());

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
        data.put("note", "Body-fat values are estimates from the U.S. Navy and BMI formulas, not a diagnosis.");
        return data;
    }

    private static float calculateBodyFat(String gender, float weight, float height, float waist, float hip, float neck) {
        // Calculate body fat percentage using the U.S. Navy Method
        float bodyFatPercentage;
        if (gender.equalsIgnoreCase("male")) {
            double inchesPerCm = 1.0 / 2.54;
            bodyFatPercentage = (float) (
                    495 / (1.0324
                            - (0.19077 * Math.log10((waist - neck) * inchesPerCm))
                            + (0.15456 * Math.log10(height * inchesPerCm))) - 450
            );

        } else {
            double inchesPerCm = 1.0 / 2.54;
            bodyFatPercentage = (float) (
                    495 / (1.29579
                            - (0.35004 * Math.log10((waist + hip - neck) * inchesPerCm))
                            + (0.22100 * Math.log10(height * inchesPerCm))) - 450
            );
        }
        return round(bodyFatPercentage);
    }

    private static float calculateFatBodyMass(float weight, float bodyFatPercentage) {
        // Calculate body fat mass
        return round(weight * bodyFatPercentage / 100);
    }

    private static float calculateLeanBodyMass(float weight, float bodyFatMass) {
        // Calculate lean body mass
        return round(weight - bodyFatMass);
    }

    private static float calculateBodyFatFromBMI(String gender, float height, float weight, int age) {
        // Calculate body fat using the BMI method
        float h = height / 100;
        float bmi = weight / (h * h); // BMI formula: weight (kg) / height (cm) ^ 2
        if (gender.equalsIgnoreCase("male")) {
            // Formula for males
            return round((float) ((1.20 * bmi) + (0.23 * age) - 16.2));

        } else {
            // Formula for females
            return round((float) ((1.20 * bmi) + (0.23 * age) - 5.4));
        }
    }

    private static float round(float value) {
        return Math.round(value * 100f) / 100f;
    }
}
