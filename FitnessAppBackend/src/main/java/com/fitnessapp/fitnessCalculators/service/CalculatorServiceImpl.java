package com.fitnessapp.fitnessCalculators.service;

import lombok.extern.slf4j.Slf4j;
import com.fitnessapp.fitnessCalculators.shared.ClientCalculatorUtils;
import com.fitnessapp.fitnessCalculators.model.dtos.requestDto.BodyFatRequestDto;
import com.fitnessapp.fitnessCalculators.model.dtos.requestDto.BurnedCaloriesFromActivityRequestDto;
import com.fitnessapp.fitnessCalculators.model.dtos.requestDto.DailyCaloriesRequestDto;
import com.fitnessapp.fitnessCalculators.model.dtos.responseDto.*;
import com.fitnessapp.fitnessCalculators.model.entities.BurnedCaloriesActivityEntity;
import com.fitnessapp.portal.dev.assetUploadDev.model.AppMediaAssetEntity;
import com.fitnessapp.fitnessCalculators.model.entities.CaloriesInFoodEntity;
import com.fitnessapp.fitnessCalculators.repo.BurnedCaloriesFromActivityRepository;
import com.fitnessapp.fitnessCalculators.repo.CaloriesInFoodRepository;
import com.fitnessapp.portal.dev.assetUploadDev.repo.AppMediaAssetRepository;
import com.fitnessapp.fitnessCalculators.service.calculatorServiceUtilities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CalculatorServiceImpl implements CalculatorService {

    private final BurnedCaloriesFromActivityRepository burnedCaloriesFromActivityRepository;
    private final CaloriesInFoodRepository caloriesInFoodRepository;
    private final AppMediaAssetRepository imageAndAboutRepository;

    @Autowired
    public CalculatorServiceImpl(BurnedCaloriesFromActivityRepository burnedCaloriesFromActivityRepository, CaloriesInFoodRepository caloriesInFoodRepository, AppMediaAssetRepository imageAndAboutRepository) {
        this.burnedCaloriesFromActivityRepository = burnedCaloriesFromActivityRepository;
        this.caloriesInFoodRepository = caloriesInFoodRepository;
        this.imageAndAboutRepository = imageAndAboutRepository;
    }

    @Override
    public ResponseEntity<?> getBmi(float weightKg, float heightCm) {
        try {

            AppMediaAssetEntity bmiData = getImageUrlAndAbout("Bmi");

            float h = heightCm / 100;
            float bmi = Float.parseFloat(String.format("%.1f", weightKg / (h * h)));

            BmiCalculateResponseDto bmiCalculateResponse = BmiCalculateResponseDto
                    .builder()
                    .status(ClientCalculatorUtils.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .bmi(bmi)
                    .healthStatus(BmiCalculatorUtilities.determineHealthStatus(bmi))
                    .healthy_bmi_range("18.5 - 25")
                    .imageUrl(bmiData.getImageUrl())
                    //      .aboutBmi(bmiData.getAbout())
                    .build();

            return ResponseEntity.ok(bmiCalculateResponse);
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ClientCalculatorUtils.errorDto(e));
        }
    }


    @Override
    public ResponseEntity<?> getBodyFat(BodyFatRequestDto bodyFatRequestDto) {
        try {
            AppMediaAssetEntity bodyFatData = getImageUrlAndAbout("BodyFat");

            BodyFatResponseDto dailyCaloriesResponseDto = BodyFatResponseDto
                    .builder()
                    .status(ClientCalculatorUtils.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .data(BodyFatCalculatorUtilities.createMap(bodyFatRequestDto))
                    .imageUrl(bodyFatData.getImageUrl())
                    .maleUrl("test male")
                    .femaleUrl("test female")
                    //       .aboutBodyFat(bodyFatData.getAbout())
                    .build();
            return ResponseEntity.ok(dailyCaloriesResponseDto);
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ClientCalculatorUtils.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getBurnedCaloriesFromActivity(BurnedCaloriesFromActivityRequestDto burnedCaloriesFromActivityRequestDto) {
        try {
            AppMediaAssetEntity burnedActivityData = getImageUrlAndAbout("BurnedActivity");

            BurnedCaloriesFromActivityResponseDto dailyCaloriesResponseDto = BurnedCaloriesFromActivityResponseDto
                    .builder()
                    .status(ClientCalculatorUtils.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .burnedCalories(BurnedCaloriesFromActivityCalculatorUtilities.createMap(burnedCaloriesFromActivityRequestDto, burnedCaloriesFromActivityRepository))
                    .imageUrl(burnedActivityData.getImageUrl())
                    //             .aboutBurnedCalories(burnedActivityData.getAbout())
                    .build();
            return ResponseEntity.ok(dailyCaloriesResponseDto);
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ClientCalculatorUtils.errorDto(e));
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
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ClientCalculatorUtils.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getCaloriesInFood(String query, double quantityGm) {
        try {
            AppMediaAssetEntity caloriesInFoodData = getImageUrlAndAbout("CaloriesInFood");

            CaloriesInFoodResponseDto dailyCaloriesResponseDto = CaloriesInFoodResponseDto
                    .builder()
                    .status(ClientCalculatorUtils.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .foodData(CaloriesInFoodCalculatorUtilities.createMap(query, quantityGm, caloriesInFoodRepository))
                    .imageUrl(caloriesInFoodData.getImageUrl())
                    //   .aboutCaloriesInFood(caloriesInFoodData.getAbout())
                    .build();
            return ResponseEntity.ok(dailyCaloriesResponseDto);
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ClientCalculatorUtils.errorDto(e));
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
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ClientCalculatorUtils.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getDailyCalories(DailyCaloriesRequestDto dailyCaloriesRequestDto) {
        try {
            AppMediaAssetEntity dailyCaloriesData = getImageUrlAndAbout("DailyCalories");

            DailyCaloriesResponseDto dailyCaloriesResponseDto = DailyCaloriesResponseDto
                    .builder()
                    .status(ClientCalculatorUtils.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .goals(DailyCaloriesCalculatorUtilities.createMap(dailyCaloriesRequestDto))
                    .imageUrl(dailyCaloriesData.getImageUrl())
                    //   .aboutDailyCalories(dailyCaloriesData.getAbout())
                    .build();
            return ResponseEntity.ok(dailyCaloriesResponseDto);
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ClientCalculatorUtils.errorDto(e));
        }
    }

    @Override
    public ResponseEntity<?> getIdealWeight(String gender, float height) {
        try {
            AppMediaAssetEntity idealWeightData = getImageUrlAndAbout("IdealWeight");

            IdealWeightResponseDto idealWeightResponseDto = IdealWeightResponseDto
                    .builder()
                    .status(ClientCalculatorUtils.SUCCESS_API_STATUS)
                    .statusCode(200)
                    .idealWeight(IdealWeightCalculatorUtilities.createMap(gender, height))
                    .imageUrl(idealWeightData.getImageUrl())
                    //      .aboutIdealWeight(idealWeightData.getAbout())
                    .build();
            return ResponseEntity.ok(idealWeightResponseDto);
        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ClientCalculatorUtils.errorDto(e));
        }
    }


    private AppMediaAssetEntity getImageUrlAndAbout(String name) {
//        try {
//            return imageAndAboutRepository.findByNameIgnoreCase(name).orElseThrow(() -> new RuntimeException("Item not found"));
//        } catch (Exception e) {
//            return new AppMediaAssetEntity(0L, "Error Getting Name", "Error Getting Image", "Error Getting About");
//        }
        return null;
    }
}
