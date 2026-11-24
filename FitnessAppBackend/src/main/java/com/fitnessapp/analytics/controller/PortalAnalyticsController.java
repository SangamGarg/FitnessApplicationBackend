package com.fitnessapp.analytics.controller;

import com.fitnessapp.analytics.model.dtos.requestDto.CalculatorCatalogRequestDto;
import com.fitnessapp.analytics.model.dtos.responseDto.CalculatorCatalogResponseDto;
import com.fitnessapp.analytics.model.dtos.responseDto.CalculatorCatalogItemResponseDto;
import com.fitnessapp.analytics.service.PortalAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_ANALYTICS_ENDPOINT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_DEV_ANALYTICS_ENDPOINT)
public class PortalAnalyticsController {
    private final PortalAnalyticsService portalAnalyticsService;

    @GetMapping("/catalog")
    public ResponseEntity<List<CalculatorCatalogItemResponseDto>> getCalculatorCatalog() {
        log.info("Request to get calculator catalog");
        return ResponseEntity.ok(portalAnalyticsService.getCalculatorCatalog());
    }

    @PostMapping("/catalog")
    public ResponseEntity<CalculatorCatalogResponseDto> addCalculatorCatalog(@RequestBody CalculatorCatalogRequestDto request) {
        log.info("Request to add calculator catalog item: {}", request.getKey());

        return ResponseEntity.status(HttpStatus.CREATED).body(portalAnalyticsService.addCalculatorCatalog(request));
    }

    @PatchMapping("/{key}")
    public ResponseEntity<CalculatorCatalogResponseDto> patchCalculatorCatalog(@PathVariable String key, @RequestBody CalculatorCatalogRequestDto request) {
        log.info("Request to patch calculator catalog item: {}", key);
        return ResponseEntity.status(HttpStatus.OK).body(portalAnalyticsService.patchCalculatorCatalog(key, request));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<CalculatorCatalogResponseDto> deleteCalculatorCatalog(@PathVariable String key) {
        log.info("Request to delete calculator catalog item: {}", key);
        return ResponseEntity.ok(portalAnalyticsService.deleteCalculatorCatalog(key));
    }
}
