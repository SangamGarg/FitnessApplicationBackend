package com.fitnessapp.analytics.controller;

import com.fitnessapp.analytics.model.SmartCalculationRequest;
import com.fitnessapp.analytics.model.dtos.responseDto.AnalyticsHomeResponseDto;
import com.fitnessapp.analytics.service.AnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Map;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_ANALYTICS_ENDPOINT;

@RestController
@RequestMapping(APP_ANALYTICS_ENDPOINT)
@Validated
@RequiredArgsConstructor
@Slf4j
public class AppAnalyticsController {
    private final AnalyticsService analyticsService;


    @GetMapping("/home")
    public ResponseEntity<AnalyticsHomeResponseDto> getAnalyticsHome() {
        log.info("Fetching analytics home screen configuration");
        return ResponseEntity.ok(analyticsService.getAnalyticsHome());
    }

    @GetMapping("/catalog")
    public ResponseEntity<?> getCatalog() {
        log.info("Fetching smart calculator catalog");
        var catalog = analyticsService.getCatalog();
        log.debug("Smart calculator catalog fetched successfully");
        return ResponseEntity.ok(catalog);
    }

    @PostMapping("/{calculatorKey}")
    public ResponseEntity<Map<String, Object>> calculate(
            @PathVariable String calculatorKey,
            @Valid @RequestBody SmartCalculationRequest request) {
        log.info("Executing smart calculation for calculatorKey='{}' with input keys={}",
                calculatorKey, request.inputs() != null ? request.inputs().keySet() : "null");
        log.debug("Smart calculation payload for calculatorKey='{}': {}", calculatorKey, request);

        Map<String, Object> result = analyticsService.calculate(calculatorKey, request.inputs());

        log.info("Completed smart calculation for calculatorKey='{}'", calculatorKey);
        log.debug("Calculation result for calculatorKey='{}': {}", calculatorKey, result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/fitness-score")
    public ResponseEntity<Map<String, Object>> getFitnessScore(
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        log.info("Fetching fitness score for timeZone='{}'", timeZone);
        try {
            ZoneId zoneId = ZoneId.of(timeZone);
            Map<String, Object> fitnessScore = analyticsService.getFitnessScore(zoneId);
            log.info("Successfully calculated fitness score for zoneId='{}'", zoneId);
            return ResponseEntity.ok(fitnessScore);
        } catch (DateTimeException ex) {
            log.warn("Invalid timeZone provided: '{}'. Error: {}", timeZone, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timeZone. Use an IANA zone such as Asia/Kolkata.", ex);
        }
    }

}
