package com.fitnessapp.analytics.controller;

import com.fitnessapp.analytics.model.dtos.requestDto.CalculatorCatalogRequestDto;
import com.fitnessapp.analytics.model.dtos.responseDto.CalculatorCatalogResponseDto;
import com.fitnessapp.analytics.model.entity.CalculatorCatalogEntity;
import com.fitnessapp.analytics.repository.CalculatorCatalogRepository;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_CALCULATOR_ENDPOINT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_CALCULATOR_ENDPOINT)
public class PortalAnalyticsController {
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


}
