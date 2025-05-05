package com.fitnessapp.fitnessCalculatorsService.services;

import com.fitnessapp.fitnessCalculatorsService.calculatorServiceUtilities.AppConstantsCalculatorService;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.BodyFatRequestDto;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.BurnedCaloriesFromActivityRequestDto;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto.DailyCaloriesRequestDto;
import com.fitnessapp.fitnessCalculatorsService.models.dtos.responseDto.*;
import com.fitnessapp.fitnessCalculatorsService.models.entities.BurnedCaloriesActivityEntity;
import com.fitnessapp.fitnessCalculatorsService.models.entities.CalculatorImageAndAboutEntity;
import com.fitnessapp.fitnessCalculatorsService.models.entities.CaloriesInFoodEntity;
import com.fitnessapp.fitnessCalculatorsService.repositories.BurnedCaloriesFromActivityRepository;
import com.fitnessapp.fitnessCalculatorsService.repositories.CaloriesInFoodRepository;
import com.fitnessapp.fitnessCalculatorsService.repositories.CalculatorImageAndAboutRepository;
import com.fitnessapp.fitnessCalculatorsService.services.calculatorServiceUtilities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalculatorServiceImpl implements CalculatorService {

    private final BurnedCaloriesFromActivityRepository burnedCaloriesFromActivityRepository;
    private final CaloriesInFoodRepository caloriesInFoodRepository;
    private final CalculatorImageAndAboutRepository imageAndAboutRepository;

    @Autowired
    public CalculatorServiceImpl(BurnedCaloriesFromActivityRepository burnedCaloriesFromActivityRepository, CaloriesInFoodRepository caloriesInFoodRepository, CalculatorImageAndAboutRepository imageAndAboutRepository) {
        this.burnedCaloriesFromActivityRepository = burnedCaloriesFromActivityRepository;
        this.caloriesInFoodRepository = caloriesInFoodRepository;
        this.imageAndAboutRepository = imageAndAboutRepository;
    }

    @Override
    public ResponseEntity<?> getBmi(float weightKg, float heightCm) {
        try {

            CalculatorImageAndAboutEntity bmiData = getImageUrlAndAbout("Bmi");

            float h = heightCm / 100;
            float bmi = Float.parseFloat(String.format("%.1f", weightKg / (h * h)));

            BmiCalculateResponseDto bmiCalculateResponse = BmiCalculateResponseDto
                    .builder()
                    .status(AppConstantsCalculatorService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .bmi(bmi)
                    .healthStatus(BmiCalculatorUtilities.determineHealthStatus(bmi))
                    .healthy_bmi_range("18.5 - 25")
                    .imageUrl(bmiData.getImageUrl())
                    .aboutBmi(bmiData.getAbout())
                    .build();

            return ResponseEntity.ok(bmiCalculateResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsCalculatorService.errorDto(e));
        }
    }


    @Override
    public ResponseEntity<?> getBodyFat(BodyFatRequestDto bodyFatRequestDto) {
        try {
            CalculatorImageAndAboutEntity bodyFatData = getImageUrlAndAbout("BodyFat");

            BodyFatResponseDto dailyCaloriesResponseDto = BodyFatResponseDto
                    .builder()
                    .status(AppConstantsCalculatorService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .data(BodyFatCalculatorUtilities.createMap(bodyFatRequestDto))
                    .imageUrl(bodyFatData.getImageUrl())
                    .maleUrl("test male")
                    .femaleUrl("test female")
                    .aboutBodyFat(bodyFatData.getAbout())
                    .build();
            return ResponseEntity.ok(dailyCaloriesResponseDto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsCalculatorService.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getBurnedCaloriesFromActivity(BurnedCaloriesFromActivityRequestDto burnedCaloriesFromActivityRequestDto) {
        try {
            CalculatorImageAndAboutEntity burnedActivityData = getImageUrlAndAbout("BurnedActivity");

            BurnedCaloriesFromActivityResponseDto dailyCaloriesResponseDto = BurnedCaloriesFromActivityResponseDto
                    .builder()
                    .status(AppConstantsCalculatorService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .burnedCalories(BurnedCaloriesFromActivityCalculatorUtilities.createMap(burnedCaloriesFromActivityRequestDto, burnedCaloriesFromActivityRepository))
                    .imageUrl(burnedActivityData.getImageUrl())
                    .aboutBurnedCalories(burnedActivityData.getAbout())
                    .build();
            return ResponseEntity.ok(dailyCaloriesResponseDto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsCalculatorService.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getBurnedCaloriesSuggestion(String query) {
        try {
            List<BurnedCaloriesActivityEntity> results = burnedCaloriesFromActivityRepository
                    .findByNameStartingWithIgnoreCase(query);

            List<String> names = results.stream()
                    .map(BurnedCaloriesActivityEntity::getName)
                    .toList();

            if (names.isEmpty()) {
                return ResponseEntity.ok(List.of());
            } else {
                return ResponseEntity.ok(names);
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsCalculatorService.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getCaloriesInFood(String query, double quantityGm) {
        try {
            CalculatorImageAndAboutEntity caloriesInFoodData = getImageUrlAndAbout("CaloriesInFood");

            CaloriesInFoodResponseDto dailyCaloriesResponseDto = CaloriesInFoodResponseDto
                    .builder()
                    .status(AppConstantsCalculatorService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .foodData(CaloriesInFoodCalculatorUtilities.createMap(query, quantityGm, caloriesInFoodRepository))
                    .imageUrl(caloriesInFoodData.getImageUrl())
                    .aboutCaloriesInFood(caloriesInFoodData.getAbout())
                    .build();
            return ResponseEntity.ok(dailyCaloriesResponseDto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsCalculatorService.errorDto(e));
        }
    }
    @Override
    public ResponseEntity<?> getCaloriesInFoodSuggestion(String query) {
        try {
            List<CaloriesInFoodEntity> results = caloriesInFoodRepository
                    .findByNameStartingWithIgnoreCase(query);

            List<String> names = results.stream()
                    .map(CaloriesInFoodEntity::getName)
                    .toList();

            if (names.isEmpty()) {
                return ResponseEntity.ok(List.of());
            } else {
                return ResponseEntity.ok(names);
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsCalculatorService.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getDailyCalories(DailyCaloriesRequestDto dailyCaloriesRequestDto) {
        try {
            CalculatorImageAndAboutEntity dailyCaloriesData = getImageUrlAndAbout("DailyCalories");

            DailyCaloriesResponseDto dailyCaloriesResponseDto = DailyCaloriesResponseDto
                    .builder()
                    .status(AppConstantsCalculatorService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .goals(DailyCaloriesCalculatorUtilities.createMap(dailyCaloriesRequestDto))
                    .imageUrl(dailyCaloriesData.getImageUrl())
                    .aboutDailyCalories(dailyCaloriesData.getAbout())
                    .build();
            return ResponseEntity.ok(dailyCaloriesResponseDto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsCalculatorService.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getIdealWeight(String gender, float height) {
        try {
            CalculatorImageAndAboutEntity idealWeightData = getImageUrlAndAbout("IdealWeight");

            IdealWeightResponseDto idealWeightResponseDto = IdealWeightResponseDto
                    .builder()
                    .status(AppConstantsCalculatorService.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .idealWeight(IdealWeightCalculatorUtilities.createMap(gender, height))
                    .imageUrl(idealWeightData.getImageUrl())
                    .aboutIdealWeight(idealWeightData.getAbout())
                    .build();
            return ResponseEntity.ok(idealWeightResponseDto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsCalculatorService.errorDto(e));
        }
    }


    private CalculatorImageAndAboutEntity getImageUrlAndAbout(String name) {
        try {
            return imageAndAboutRepository.findByNameIgnoreCase(name).orElseThrow(() -> new RuntimeException("Item not found"));
        } catch (Exception e) {
            return new CalculatorImageAndAboutEntity(0L, "Error Getting Name", "Error Getting Image", "Error Getting About");
        }
    }

}
