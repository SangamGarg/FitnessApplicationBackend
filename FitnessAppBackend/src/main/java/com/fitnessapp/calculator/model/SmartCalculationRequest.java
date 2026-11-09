package com.fitnessapp.calculator.model;

import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

/** Numeric and text inputs used by the smart calculator endpoints. */
public record SmartCalculationRequest(@NotEmpty Map<String, Object> inputs) { }
