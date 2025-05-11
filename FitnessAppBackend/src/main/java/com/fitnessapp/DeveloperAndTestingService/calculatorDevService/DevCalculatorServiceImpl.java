package com.fitnessapp.DeveloperAndTestingService.calculatorDevService;

import com.fitnessapp.fitnessCalculatorsService.models.entities.BurnedCaloriesActivityEntity;
import com.fitnessapp.fitnessCalculatorsService.models.entities.CalculatorImageAndAboutEntity;
import com.fitnessapp.fitnessCalculatorsService.models.entities.CaloriesInFoodEntity;
import com.fitnessapp.fitnessCalculatorsService.repositories.BurnedCaloriesFromActivityRepository;
import com.fitnessapp.fitnessCalculatorsService.repositories.CalculatorImageAndAboutRepository;
import com.fitnessapp.fitnessCalculatorsService.repositories.CaloriesInFoodRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;

@Service
public class DevCalculatorServiceImpl implements DevCalculatorService {

    private final CalculatorImageAndAboutRepository imageAndAboutRepository;
    private final CaloriesInFoodRepository caloriesInFoodRepository;
    private final BurnedCaloriesFromActivityRepository burnedCaloriesFromActivityRepository;

    public DevCalculatorServiceImpl(CalculatorImageAndAboutRepository imageAndAboutRepository, CaloriesInFoodRepository caloriesInFoodRepository, BurnedCaloriesFromActivityRepository burnedCaloriesFromActivityRepository) {

        this.imageAndAboutRepository = imageAndAboutRepository;
        this.caloriesInFoodRepository = caloriesInFoodRepository;
        this.burnedCaloriesFromActivityRepository = burnedCaloriesFromActivityRepository;
    }

    @Transactional
    @Override
    public ResponseEntity<?> uploadCalculatorImageAndAbout(DevCalculatorUploadImageAndAboutRequestDto dto) {

        if (dto.getAbout() == null || dto.getAbout().isBlank()) {
            return ResponseEntity.badRequest().body("About is Null or Blank");
        }
        if (dto.getImageUrl() == null || dto.getImageUrl().isBlank()) {
            return ResponseEntity.badRequest().body("Image Url is Null or Blank");
        }
        try {
            imageAndAboutRepository.save(CalculatorImageAndAboutEntity.
                    builder()
                    .name(dto.getName())
                    .imageUrl(dto.getImageUrl())
                    .about(dto.getAbout())
                    .build());
            return ResponseEntity.ok("Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> patchCalculatorImageAndAbout(DevCalculatorUploadImageAndAboutRequestDto dto) {
        try {
            var existing = imageAndAboutRepository.findByNameIgnoreCase(dto.getName());
            if (existing.isEmpty()) {
                return ResponseEntity.badRequest().body("Calculator with the given name not found");
            }

            CalculatorImageAndAboutEntity entity = existing.get();

            if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
                entity.setImageUrl(dto.getImageUrl());
            }

            if (dto.getAbout() != null && !dto.getAbout().isBlank()) {
                entity.setAbout(dto.getAbout());
            }

            imageAndAboutRepository.save(entity);
            return ResponseEntity.ok("Updated Successfully");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> uploadCalculatorCaloriesInFood(List<DevCalculatorUploadCaloriesInFoodRequestDto> requestDto) {
        for (DevCalculatorUploadCaloriesInFoodRequestDto dto : requestDto) {
            for (Field field : DevCalculatorUploadCaloriesInFoodRequestDto.class.getDeclaredFields()) {
                field.setAccessible(true); // in case the field is private
                try {
                    Object value = field.get(dto);
                    if (value instanceof String && ((String) value).isBlank()) {
                        return ResponseEntity.badRequest().body(field.getName() + " is Null or Blank");
                    }
                    if (value == null) {
                        return ResponseEntity.badRequest().body(field.getName() + " is Null");
                    }
                } catch (IllegalAccessException e) {
                    return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
                }
            }
        }

        try {
            List<CaloriesInFoodEntity> caloriesInFoodEntities = requestDto.stream()
                    .map(dto -> CaloriesInFoodEntity.builder()
                            .name(dto.getName())
                            .calories(dto.getCalories())
                            .serving_size_g(dto.getServingSizeG())
                            .protein_g(dto.getProteinG())
                            .carbohydrates_total_g(dto.getCarbohydratesTotalG())
                            .fiber_g(dto.getFiberG())
                            .fat_saturated_g(dto.getFatSaturatedG())
                            .fat_total_g(dto.getFatTotalG())
                            .cholesterol_mg(dto.getCholesterolMg())
                            .sugar_g(dto.getSugarG())
                            .sodium_mg(dto.getSodiumMg())
                            .potassium_mg(dto.getPotassiumMg())
                            .imageUrl(dto.getImageUrl())
                            .build())
                    .toList();

            caloriesInFoodRepository.saveAll(caloriesInFoodEntities);

            return ResponseEntity.ok("Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> patchCalculatorCaloriesInFood(DevCalculatorUploadCaloriesInFoodRequestDto dto) {
        try {

            var existingOptional = caloriesInFoodRepository.findByNameIgnoreCase(dto.getName());

            if (existingOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Food item with the given name not found");
            }

            CaloriesInFoodEntity entity = existingOptional.get();

            if (dto.getCalories() != null) entity.setCalories(dto.getCalories());
            if (dto.getServingSizeG() != null) entity.setServing_size_g(dto.getServingSizeG());
            if (dto.getProteinG() != null) entity.setProtein_g(dto.getProteinG());
            if (dto.getCarbohydratesTotalG() != null) entity.setCarbohydrates_total_g(dto.getCarbohydratesTotalG());
            if (dto.getFiberG() != null) entity.setFiber_g(dto.getFiberG());
            if (dto.getFatSaturatedG() != null) entity.setFat_saturated_g(dto.getFatSaturatedG());
            if (dto.getFatTotalG() != null) entity.setFat_total_g(dto.getFatTotalG());
            if (dto.getCholesterolMg() != null) entity.setCholesterol_mg(dto.getCholesterolMg());
            if (dto.getSugarG() != null) entity.setSugar_g(dto.getSugarG());
            if (dto.getSodiumMg() != null) entity.setSodium_mg(dto.getSodiumMg());
            if (dto.getPotassiumMg() != null) entity.setPotassium_mg(dto.getPotassiumMg());
            if (dto.getImageUrl() != null) entity.setImageUrl(dto.getImageUrl());

            caloriesInFoodRepository.save(entity);

            return ResponseEntity.ok("Updated Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> uploadCalculatorBurnedCalories(List<DevCalculatorBurnedCaloriesRequestDto> requestDto) {
        for (DevCalculatorBurnedCaloriesRequestDto dto : requestDto) {
            if (dto.getMetValue() == null) {
                return ResponseEntity.badRequest().body("Met Value is Null");
            }
            if (dto.getImageUrl() == null || dto.getImageUrl().isBlank()) {
                return ResponseEntity.badRequest().body("Image Url is Null or Blank");
            }
        }

        try {
            List<BurnedCaloriesActivityEntity> burnedCaloriesActivityEntities = requestDto.stream()
                    .map(dto -> BurnedCaloriesActivityEntity.builder()
                            .name(dto.getName())
                            .metValue(dto.getMetValue())
                            .imageUrl(dto.getImageUrl())
                            .build())
                    .toList();

            burnedCaloriesFromActivityRepository.saveAll(burnedCaloriesActivityEntities);

            return ResponseEntity.ok("Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> patchCalculatorBurnedCalories(DevCalculatorBurnedCaloriesRequestDto dto) {
        try {

            var existingOptional = burnedCaloriesFromActivityRepository.findByNameIgnoreCase(dto.getName());

            if (existingOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Activity with the given name not found");
            }

            BurnedCaloriesActivityEntity entity = existingOptional.get();

            if (dto.getMetValue() != null) entity.setMetValue(dto.getMetValue());
            if (dto.getImageUrl() != null) entity.setImageUrl(dto.getImageUrl());

            burnedCaloriesFromActivityRepository.save(entity);

            return ResponseEntity.ok("Updated Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

}
