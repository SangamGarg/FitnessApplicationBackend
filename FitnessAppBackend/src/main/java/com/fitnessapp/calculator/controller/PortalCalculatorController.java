package com.fitnessapp.calculator.controller;

import com.fitnessapp.calculator.portal.model.DevCalculatorBurnedCaloriesRequestDto;
import com.fitnessapp.calculator.portal.model.DevCalculatorUploadCaloriesInFoodRequestDto;
import com.fitnessapp.calculator.portal.service.PortalCalculatorService;
import com.fitnessapp.calculator.model.CalculatorCatalogRequestDto;
import com.fitnessapp.calculator.model.CalculatorCatalogResponseDto;
import com.fitnessapp.calculator.model.entities.CalculatorCatalogEntity;
import com.fitnessapp.calculator.repository.CalculatorCatalogRepository;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_CALCULATOR_ENDPOINT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_CALCULATOR_ENDPOINT)
public class PortalCalculatorController {
    private final PortalCalculatorService portalCalculatorService;
    private final CalculatorCatalogRepository catalogRepository;

    @PostMapping("/catalog")
    public ResponseEntity<CalculatorCatalogResponseDto> addCatalog(@RequestBody CalculatorCatalogRequestDto request) {
        log.info("Request to add calculator catalog item: {}", request.getKey());

        if (catalogRepository.existsById(request.getKey())) {
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

        catalogRepository.save(entity);

        return ResponseEntity.status(HttpStatus.CREATED).body(CalculatorCatalogResponseDto.builder()
                .status(AppResponseStatus.SUCCESS.name())
                .statusCode(201)
                .message("Calculator added successfully")
                .build());
    }

    @PatchMapping("/{key}")
    public ResponseEntity<CalculatorCatalogResponseDto> patchCatalog(@PathVariable String key, @RequestBody CalculatorCatalogRequestDto request) {
        log.info("Request to patch calculator catalog item: {}", key);

        CalculatorCatalogEntity entity = catalogRepository.findById(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Calculator not found: " + key));

        if (request.getName() != null) entity.setName(request.getName());
        if (request.getCategory() != null) entity.setCategory(request.getCategory());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getRequiredInputs() != null) entity.setRequiredInputs(request.getRequiredInputs());
        if (request.getUnit() != null) entity.setUnit(request.getUnit());
        if (request.getRequiresSavedData() != null) entity.setRequiresSavedData(request.getRequiresSavedData());

        catalogRepository.save(entity);

        return ResponseEntity.ok(CalculatorCatalogResponseDto.builder()
                .status(AppResponseStatus.SUCCESS.name())
                .statusCode(200)
                .message("Calculator updated successfully")
                .build());
    }


    @PostMapping("/upload-calories-in-food")
    public ResponseEntity<?> uploadCaloriesInFood(
            @RequestBody @NotEmpty(message = "List cannot be empty")
            List<@Valid DevCalculatorUploadCaloriesInFoodRequestDto> requestDto) {

        log.info("POST upload-calories-in-food received: itemCount={}", requestDto.size());
        if (log.isTraceEnabled()) {
            log.trace("Food names in upload payload: {}",
                    requestDto.stream().map(DevCalculatorUploadCaloriesInFoodRequestDto::getName).toList());
        }

        ResponseEntity<?> response = portalCalculatorService.uploadCalculatorCaloriesInFood(requestDto);

        log.debug("POST upload-calories-in-food completed: status={}", response.getStatusCode().value());
        return response;
    }

    @PatchMapping("/patch-calories-in-food")
    public ResponseEntity<?> patchCaloriesInFood(
            @RequestBody @Valid DevCalculatorUploadCaloriesInFoodRequestDto requestDto) {

        log.info("PATCH patch-calories-in-food received: name='{}'", requestDto.getName());
        log.trace("Food patch payload: {}", requestDto);

        ResponseEntity<?> response = portalCalculatorService.patchCalculatorCaloriesInFood(requestDto);

        log.debug("PATCH patch-calories-in-food completed: name='{}', status={}",
                requestDto.getName(), response.getStatusCode().value());
        return response;
    }


    @PostMapping("/upload-burned-calories")
    public ResponseEntity<?> uploadBurnedCalories(
            @RequestBody @NotEmpty(message = "List cannot be empty")
            List<@Valid DevCalculatorBurnedCaloriesRequestDto> requestDto) {

        log.info("POST upload-burned-calories received: itemCount={}", requestDto.size());
        if (log.isTraceEnabled()) {
            log.trace("Activity names in upload payload: {}",
                    requestDto.stream().map(DevCalculatorBurnedCaloriesRequestDto::getName).toList());
        }

        ResponseEntity<?> response = portalCalculatorService.uploadCalculatorBurnedCalories(requestDto);

        log.debug("POST upload-burned-calories completed: status={}", response.getStatusCode().value());
        return response;
    }

    @PatchMapping("/patch-burned-calories")
    public ResponseEntity<?> patchBurnedCalories(
            @RequestBody @Valid DevCalculatorBurnedCaloriesRequestDto requestDto) {

        log.info("PATCH patch-burned-calories received: name='{}'", requestDto.getName());
        log.trace("Burned-calories patch payload: {}", requestDto);

        ResponseEntity<?> response = portalCalculatorService.patchCalculatorBurnedCalories(requestDto);

        log.debug("PATCH patch-burned-calories completed: name='{}', status={}",
                requestDto.getName(), response.getStatusCode().value());
        return response;
    }
}
