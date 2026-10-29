package com.fitnessapp.calculator.service.calculatorServiceUtilities;

public final class BmiCalculatorUtilities {
    public static String determineHealthStatus(float bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi < 25.0) {
            return "Normal";
        } else if (bmi < 30.0) {
            return "Overweight";
        } else if (bmi < 35.0) {
            return "Obesity Class 1";
        } else if (bmi < 40.0) {
            return "Obesity Class 2";
        } else {
            return "Obesity Class 3";
        }
    }
}
