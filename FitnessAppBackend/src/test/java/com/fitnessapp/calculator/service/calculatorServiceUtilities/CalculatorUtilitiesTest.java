package com.fitnessapp.calculator.service.calculatorServiceUtilities;

import com.fitnessapp.calculator.model.dtos.requestDto.BodyFatRequestDto;
import com.fitnessapp.calculator.model.dtos.requestDto.BurnedCaloriesFromActivityRequestDto;
import com.fitnessapp.calculator.model.dtos.requestDto.DailyCaloriesRequestDto;
import com.fitnessapp.calculator.model.entities.BurnedCaloriesActivityEntity;
import com.fitnessapp.calculator.model.entities.CaloriesInFoodEntity;
import com.fitnessapp.calculator.repo.BurnedCaloriesFromActivityRepository;
import com.fitnessapp.calculator.repo.CaloriesInFoodRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalculatorUtilitiesTest {

    @Test
    void dailyCaloriesUsesMifflinStJeorAndExplicitActivityMultiplier() {
        DailyCaloriesRequestDto request = DailyCaloriesRequestDto.builder()
                .age(30)
                .gender("male")
                .weightKg(70f)
                .heightCm(175f)
                .activityLevel("level_3")
                .build();

        Map<String, Object> result = DailyCaloriesCalculatorUtilities.createMap(request);

        assertEquals(1648, result.get("BMR"));
        assertEquals(1.55, result.get("activityMultiplier"));
        assertTrue(((String) result.get("note")).contains("estimates"));
    }

    @Test
    void bmiCategoriesHaveNoBoundaryGaps() {
        assertEquals("Normal", BmiCalculatorUtilities.determineHealthStatus(24.9f));
        assertEquals("Overweight", BmiCalculatorUtilities.determineHealthStatus(25f));
        assertEquals("Overweight", BmiCalculatorUtilities.determineHealthStatus(29.9f));
        assertEquals("Obesity Class 1", BmiCalculatorUtilities.determineHealthStatus(30f));
        assertEquals("Obesity Class 2", BmiCalculatorUtilities.determineHealthStatus(35f));
        assertEquals("Obesity Class 3", BmiCalculatorUtilities.determineHealthStatus(40f));
    }

    @Test
    void bodyFatNavyEstimateUsesConvertedInchesAndReturnsFiniteResults() {
        BodyFatRequestDto request = BodyFatRequestDto.builder()
                .age(30)
                .gender("male")
                .weightKg(80f)
                .heightCm(177.8f)
                .waistCm(86.36f)
                .hipCm(95f)
                .neckCm(38.1f)
                .build();

        Map<String, Object> result = BodyFatCalculatorUtilities.createMap(request);

        float navy = (float) result.get("BodyFatPercentage (U.S.NAVY)");
        assertTrue(Float.isFinite(navy));
        assertTrue(navy > 0f && navy < 40f);
        assertTrue(result.containsKey("note"));
    }

    @Test
    void activityCaloriesUseMetFormulaWithTheThreePointFiveFactor() {
        BurnedCaloriesFromActivityRepository repository = mock(BurnedCaloriesFromActivityRepository.class);
        when(repository.findByNameIgnoreCase("running"))
                .thenReturn(Optional.of(BurnedCaloriesActivityEntity.builder()
                        .name("running")
                        .metValue(8.0)
                        .build()));
        BurnedCaloriesFromActivityRequestDto request = BurnedCaloriesFromActivityRequestDto.builder()
                .activityName("running")
                .weightKg(70f)
                .durationMin(30)
                .build();

        Map<String, Object> result = BurnedCaloriesFromActivityCalculatorUtilities.createMap(request, repository);

        assertEquals(1960, result.get("calories_per_hour"));
        assertEquals(980, result.get("total_calories"));
    }

    @Test
    void foodNutritionScalesValuesFromConfiguredServingSize() {
        CaloriesInFoodRepository repository = mock(CaloriesInFoodRepository.class);
        CaloriesInFoodEntity food = CaloriesInFoodEntity.builder()
                .name("oats")
                .serving_size_g(100.0)
                .calories(380.0)
                .protein_g(10.0)
                .build();
        when(repository.findByNameIgnoreCase("oats")).thenReturn(Optional.of(food));

        Map<String, Object> result = CaloriesInFoodCalculatorUtilities.createMap("oats", 150.0, repository);

        assertEquals(570.0, result.get("calories"));
        assertEquals(15.0, result.get("protein_g"));
        assertFalse(result.containsKey("fat_total_g"));
        assertTrue(((String) result.get("nutrition_values_are_for")).contains("100.0 g"));
    }
}
