package com.fitnessapp.portal.dev.calculatorDev;

import com.fitnessapp.fitnessCalculators.models.entities.BurnedCaloriesActivityEntity;
import com.fitnessapp.fitnessCalculators.models.entities.CaloriesInFoodEntity;
import com.fitnessapp.fitnessCalculators.repositories.BurnedCaloriesFromActivityRepository;
import com.fitnessapp.fitnessCalculators.repositories.CaloriesInFoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Dev-only service for seeding and patching calculator reference data.
 * <p>
 * Unexpected persistence failures are intentionally <b>not</b> caught here: they propagate so the
 * transaction rolls back cleanly and {@link DevCalculatorExceptionHandler} logs and maps them once.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DevCalculatorServiceImpl implements DevCalculatorService {

    private final CaloriesInFoodRepository caloriesInFoodRepository;
    private final BurnedCaloriesFromActivityRepository burnedCaloriesFromActivityRepository;

    // ------------------------------------------------------------------------------------------
    // Calories in food
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional
    public ResponseEntity<?> uploadCalculatorCaloriesInFood(List<DevCalculatorUploadCaloriesInFoodRequestDto> requestDto) {
        log.debug("Validating calories-in-food upload: itemCount={}", requestDto.size());

        for (DevCalculatorUploadCaloriesInFoodRequestDto dto : requestDto) {
            List<String> missing = findMissingFoodFields(dto);
            if (!missing.isEmpty()) {
                log.warn("Calories-in-food upload rejected: name='{}', missingFields={}", dto.getName(), missing);
                return ResponseEntity.badRequest()
                        .body("Missing required field(s) for '" + dto.getName() + "': " + String.join(", ", missing));
            }
        }

        List<String> duplicatesInRequest = findDuplicateNames(requestDto, DevCalculatorUploadCaloriesInFoodRequestDto::getName);
        if (!duplicatesInRequest.isEmpty()) {
            log.warn("Calories-in-food upload rejected: duplicate names in request {}", duplicatesInRequest);
            return ResponseEntity.badRequest().body("Duplicate name(s) in request: " + duplicatesInRequest);
        }

        for (DevCalculatorUploadCaloriesInFoodRequestDto dto : requestDto) {
            if (caloriesInFoodRepository.findByNameIgnoreCase(dto.getName()).isPresent()) {
                log.warn("Calories-in-food upload rejected: name='{}' already exists", dto.getName());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Food item already exists: " + dto.getName());
            }
        }

        List<CaloriesInFoodEntity> entities = requestDto.stream()
                .map(dto -> {
                    log.trace("Mapping food dto to entity: {}", dto);
                    return CaloriesInFoodEntity.builder()
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
                            .build();
                })
                .toList();

        caloriesInFoodRepository.saveAll(entities);

        log.info("Calories-in-food upload succeeded: savedCount={}", entities.size());
        return ResponseEntity.ok("Uploaded Successfully");
    }

    @Override
    @Transactional
    public ResponseEntity<?> patchCalculatorCaloriesInFood(DevCalculatorUploadCaloriesInFoodRequestDto dto) {
        log.debug("Looking up food item for patch: name='{}'", dto.getName());

        var existing = caloriesInFoodRepository.findByNameIgnoreCase(dto.getName());
        if (existing.isEmpty()) {
            log.warn("Calories-in-food patch rejected: name='{}' not found", dto.getName());
            return ResponseEntity.badRequest().body("Food item with the given name not found");
        }

        CaloriesInFoodEntity entity = existing.get();
        List<String> changed = new ArrayList<>();

        applyIfPresent(dto.getCalories(), entity::setCalories, "calories", changed);
        applyIfPresent(dto.getServingSizeG(), entity::setServing_size_g, "servingSizeG", changed);
        applyIfPresent(dto.getProteinG(), entity::setProtein_g, "proteinG", changed);
        applyIfPresent(dto.getCarbohydratesTotalG(), entity::setCarbohydrates_total_g, "carbohydratesTotalG", changed);
        applyIfPresent(dto.getFiberG(), entity::setFiber_g, "fiberG", changed);
        applyIfPresent(dto.getFatSaturatedG(), entity::setFat_saturated_g, "fatSaturatedG", changed);
        applyIfPresent(dto.getFatTotalG(), entity::setFat_total_g, "fatTotalG", changed);
        applyIfPresent(dto.getCholesterolMg(), entity::setCholesterol_mg, "cholesterolMg", changed);
        applyIfPresent(dto.getSugarG(), entity::setSugar_g, "sugarG", changed);
        applyIfPresent(dto.getSodiumMg(), entity::setSodium_mg, "sodiumMg", changed);
        applyIfPresent(dto.getPotassiumMg(), entity::setPotassium_mg, "potassiumMg", changed);
        applyIfPresent(textOrNull(dto.getImageUrl()), entity::setImageUrl, "imageUrl", changed);

        if (changed.isEmpty()) {
            log.debug("Calories-in-food patch is a no-op: name='{}' (no fields supplied)", dto.getName());
        }

        // No explicit save(): the entity is managed, so dirty checking flushes it on commit.
        log.info("Calories-in-food patch succeeded: name='{}', changedFields={}", dto.getName(), changed);
        return ResponseEntity.ok("Updated Successfully");
    }

    // ------------------------------------------------------------------------------------------
    // Burned calories
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional
    public ResponseEntity<?> uploadCalculatorBurnedCalories(List<DevCalculatorBurnedCaloriesRequestDto> requestDto) {
        log.debug("Validating burned-calories upload: itemCount={}", requestDto.size());

        for (DevCalculatorBurnedCaloriesRequestDto dto : requestDto) {
            if (dto.getMetValue() == null) {
                log.warn("Burned-calories upload rejected: name='{}', metValue is null", dto.getName());
                return ResponseEntity.badRequest().body("Met Value is Null for '" + dto.getName() + "'");
            }
            if (!StringUtils.hasText(dto.getImageUrl())) {
                log.warn("Burned-calories upload rejected: name='{}', imageUrl is null or blank", dto.getName());
                return ResponseEntity.badRequest().body("Image Url is Null or Blank for '" + dto.getName() + "'");
            }
        }

        List<String> duplicatesInRequest = findDuplicateNames(requestDto, DevCalculatorBurnedCaloriesRequestDto::getName);
        if (!duplicatesInRequest.isEmpty()) {
            log.warn("Burned-calories upload rejected: duplicate names in request {}", duplicatesInRequest);
            return ResponseEntity.badRequest().body("Duplicate name(s) in request: " + duplicatesInRequest);
        }

        for (DevCalculatorBurnedCaloriesRequestDto dto : requestDto) {
            if (burnedCaloriesFromActivityRepository.findByNameIgnoreCase(dto.getName()).isPresent()) {
                log.warn("Burned-calories upload rejected: name='{}' already exists", dto.getName());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Activity already exists: " + dto.getName());
            }
        }

        List<BurnedCaloriesActivityEntity> entities = requestDto.stream()
                .map(dto -> {
                    log.trace("Mapping burned-calories dto to entity: {}", dto);
                    return BurnedCaloriesActivityEntity.builder()
                            .name(dto.getName())
                            .metValue(dto.getMetValue())
                            .imageUrl(dto.getImageUrl())
                            .build();
                })
                .toList();

        burnedCaloriesFromActivityRepository.saveAll(entities);

        log.info("Burned-calories upload succeeded: savedCount={}", entities.size());
        return ResponseEntity.ok("Uploaded Successfully");
    }

    @Override
    @Transactional
    public ResponseEntity<?> patchCalculatorBurnedCalories(DevCalculatorBurnedCaloriesRequestDto dto) {
        log.debug("Looking up activity for patch: name='{}'", dto.getName());

        var existing = burnedCaloriesFromActivityRepository.findByNameIgnoreCase(dto.getName());
        if (existing.isEmpty()) {
            log.warn("Burned-calories patch rejected: name='{}' not found", dto.getName());
            return ResponseEntity.badRequest().body("Activity with the given name not found");
        }

        BurnedCaloriesActivityEntity entity = existing.get();
        List<String> changed = new ArrayList<>();

        applyIfPresent(dto.getMetValue(), entity::setMetValue, "metValue", changed);
        applyIfPresent(textOrNull(dto.getImageUrl()), entity::setImageUrl, "imageUrl", changed);

        if (changed.isEmpty()) {
            log.debug("Burned-calories patch is a no-op: name='{}' (no fields supplied)", dto.getName());
        }

        // No explicit save(): the entity is managed, so dirty checking flushes it on commit.
        log.info("Burned-calories patch succeeded: name='{}', changedFields={}", dto.getName(), changed);
        return ResponseEntity.ok("Updated Successfully");
    }

    // ------------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------------

    /** Returns the names of all fields that must be present on upload but are null/blank. */
    private static List<String> findMissingFoodFields(DevCalculatorUploadCaloriesInFoodRequestDto dto) {
        List<String> missing = new ArrayList<>();
        if (!StringUtils.hasText(dto.getName())) missing.add("name");
        if (dto.getCalories() == null) missing.add("calories");
        if (dto.getServingSizeG() == null) missing.add("servingSizeG");
        if (dto.getProteinG() == null) missing.add("proteinG");
        if (dto.getCarbohydratesTotalG() == null) missing.add("carbohydratesTotalG");
        if (dto.getFiberG() == null) missing.add("fiberG");
        if (dto.getFatSaturatedG() == null) missing.add("fatSaturatedG");
        if (dto.getFatTotalG() == null) missing.add("fatTotalG");
        if (dto.getCholesterolMg() == null) missing.add("cholesterolMg");
        if (dto.getSugarG() == null) missing.add("sugarG");
        if (dto.getSodiumMg() == null) missing.add("sodiumMg");
        if (dto.getPotassiumMg() == null) missing.add("potassiumMg");
        if (!StringUtils.hasText(dto.getImageUrl())) missing.add("imageUrl");
        return missing;
    }

    /** Returns names (case-insensitive) that appear more than once in the request. */
    private static <T> List<String> findDuplicateNames(List<T> items, Function<T, String> nameExtractor) {
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        for (T item : items) {
            String name = nameExtractor.apply(item);
            if (name != null && !seen.add(name.toLowerCase(Locale.ROOT))) {
                duplicates.add(name);
            }
        }
        return new ArrayList<>(duplicates);
    }

    /** Applies {@code value} through {@code setter} when non-null and records the field name. */
    private static <T> void applyIfPresent(T value, Consumer<T> setter, String fieldName, List<String> changed) {
        if (value != null) {
            setter.accept(value);
            changed.add(fieldName);
        }
    }

    /** Treats blank strings as "not supplied" so a patch cannot wipe a field with "". */
    private static String textOrNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
