package com.fitnessapp.calculator.model;

import java.util.List;

public record CalculatorDefinition(
        String key,
        String name,
        String category,
        String description,
        List<String> requiredInputs,
        String unit,
        boolean requiresSavedData
) {
}
