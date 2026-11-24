package com.fitnessapp.analytics.service;

import com.fitnessapp.analytics.model.dtos.requestDto.CalculatorCatalogRequestDto;
import com.fitnessapp.analytics.model.dtos.responseDto.CalculatorCatalogResponseDto;
import com.fitnessapp.analytics.model.dtos.responseDto.CalculatorCatalogItemResponseDto;
import com.fitnessapp.analytics.model.entity.CalculatorCatalogEntity;
import com.fitnessapp.analytics.repository.BodyMeasurementRepository;
import com.fitnessapp.analytics.repository.CalculatorCatalogRepository;
import com.fitnessapp.analytics.repository.UserCalculatorHistoryRepository;
import com.fitnessapp.analytics.utils.CalculatorUtils;
import com.fitnessapp.appAuth.repo.UserDetailsRepo;
import com.fitnessapp.gamify.activity.repository.ActivityRepository;
import com.fitnessapp.gamify.gamification.repository.UserStreakRepository;
import com.fitnessapp.gamify.gamification.repository.UserXpAccountRepository;
import com.fitnessapp.nutrition.repository.MealEntryRepository;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortalAnalyticsServiceImpl implements PortalAnalyticsService {

    private final CalculatorUtils calculatorUtils;
    private final ActivityRepository activityRepository;
    private final MealEntryRepository mealEntryRepository;
    private final UserStreakRepository userStreakRepository;
    private final UserXpAccountRepository userXpAccountRepository;
    private final CalculatorCatalogRepository calculatorCatalogRepository;
    private final UserDetailsRepo userDetailsRepo;
    private final UserCalculatorHistoryRepository historyRepository;
    private final BodyMeasurementRepository bodyMeasurementRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CalculatorCatalogItemResponseDto> getCalculatorCatalog() {
        return calculatorCatalogRepository.findAll().stream()
                .map(calculator -> CalculatorCatalogItemResponseDto.builder()
                        .key(calculator.getKey())
                        .name(calculator.getName())
                        .category(calculator.getCategory())
                        .description(calculator.getDescription())
                        .requiredInputs(calculator.getRequiredInputs())
                        .unit(calculator.getUnit())
                        .requiresSavedData(calculator.isRequiresSavedData())
                        .popular(calculator.isPopular())
                        .displayOrder(calculator.getDisplayOrder())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public CalculatorCatalogResponseDto addCalculatorCatalog(CalculatorCatalogRequestDto request) {

        if (calculatorCatalogRepository.existsById(request.getKey())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Calculator with key " + request.getKey() + " already exists");
        }

        CalculatorCatalogEntity entity = CalculatorCatalogEntity.builder()
                .key(request.getKey())
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .requiredInputs(request.getRequiredInputs())
                .unit(request.getUnit())
                .requiresSavedData(request.getRequiresSavedData() != null && request.getRequiresSavedData())
                .build();

        calculatorCatalogRepository.save(entity);

        return CalculatorCatalogResponseDto.builder()
                .status(AppResponseStatus.SUCCESS.name())
                .statusCode(201)
                .message("Calculator added successfully")
                .build();
    }

    @Transactional
    @Override
    public CalculatorCatalogResponseDto patchCalculatorCatalog(String key, CalculatorCatalogRequestDto request) {

        CalculatorCatalogEntity entity = calculatorCatalogRepository.findById(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Calculator not found: " + key));

        if (request.getName() != null) entity.setName(request.getName());
        if (request.getCategory() != null) entity.setCategory(request.getCategory());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getRequiredInputs() != null) entity.setRequiredInputs(request.getRequiredInputs());
        if (request.getUnit() != null) entity.setUnit(request.getUnit());
        if (request.getRequiresSavedData() != null) entity.setRequiresSavedData(request.getRequiresSavedData());

        calculatorCatalogRepository.save(entity);

        return CalculatorCatalogResponseDto.builder()
                .status(AppResponseStatus.SUCCESS.name())
                .statusCode(200)
                .message("Calculator updated successfully")
                .build();
    }

    @Override
    @Transactional
    public CalculatorCatalogResponseDto deleteCalculatorCatalog(String key) {
        if (!calculatorCatalogRepository.existsById(key)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Calculator not found: " + key);
        }

        calculatorCatalogRepository.deleteById(key);

        return CalculatorCatalogResponseDto.builder()
                .status(AppResponseStatus.SUCCESS.name())
                .statusCode(HttpStatus.OK.value())
                .message("Calculator deleted successfully")
                .build();
    }
}
