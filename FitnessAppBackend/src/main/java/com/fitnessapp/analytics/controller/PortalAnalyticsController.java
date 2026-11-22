package com.fitnessapp.analytics.controller;

import com.fitnessapp.analytics.model.dtos.requestDto.CalculatorCatalogRequestDto;
import com.fitnessapp.analytics.model.dtos.responseDto.CalculatorCatalogResponseDto;
import com.fitnessapp.analytics.model.entity.CalculatorCatalogEntity;
import com.fitnessapp.analytics.repository.CalculatorCatalogRepository;
import com.fitnessapp.analytics.service.AnalyticsService;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_ANALYTICS_ENDPOINT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_DEV_ANALYTICS_ENDPOINT)
public class PortalAnalyticsController {
    private final AnalyticsService analyticsService;

    @PostMapping("/catalog")
    public ResponseEntity<CalculatorCatalogResponseDto> addCatalog(@RequestBody CalculatorCatalogRequestDto request) {
        log.info("Request to add calculator catalog item: {}", request.getKey());

        return ResponseEntity.status(HttpStatus.CREATED).body(analyticsService.addCatalog(request));
    }

    @PatchMapping("/{key}")
    public ResponseEntity<CalculatorCatalogResponseDto> patchCatalog(@PathVariable String key, @RequestBody CalculatorCatalogRequestDto request) {
        log.info("Request to patch calculator catalog item: {}", key);
        return ResponseEntity.status(HttpStatus.OK).body(analyticsService.patchCatalog(key, request));
    }
}
