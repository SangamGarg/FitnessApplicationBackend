package com.fitnessapp.calculator.controller;

import com.fitnessapp.calculator.model.SmartCalculationRequest;
import com.fitnessapp.calculator.service.SmartCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Map;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_CALCULATOR_ENDPOINT;

@RestController
@RequestMapping(APP_CALCULATOR_ENDPOINT + "/smart")
@RequiredArgsConstructor
@Tag(name = "Smart Fitness Calculators", description = "Calculator catalogue, calculation engine and user fitness analytics")
public class SmartCalculatorController {
    private final SmartCalculatorService smartCalculatorService;

    @GetMapping("/catalog")
    @Operation(summary = "Get all 36 fitness calculators grouped by category")
    public ResponseEntity<?> getCatalog() {
        return ResponseEntity.ok(smartCalculatorService.getCatalog());
    }

    @PostMapping("/{calculatorKey}")
    @Operation(summary = "Run a smart fitness calculation")
    public ResponseEntity<Map<String, Object>> calculate(
            @PathVariable String calculatorKey,
            @Valid @RequestBody SmartCalculationRequest request) {
        return ResponseEntity.ok(smartCalculatorService.calculate(calculatorKey, request.inputs()));
    }

    @GetMapping("/fitness-score")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get a 7-day fitness engagement score using saved activity and nutrition data")
    public ResponseEntity<Map<String, Object>> getFitnessScore(
            Authentication authentication,
            @RequestParam(defaultValue = "Asia/Kolkata") String timeZone) {
        if (authentication == null || authentication.getName() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        try {
            return ResponseEntity.ok(smartCalculatorService.getFitnessScore(authentication.getName(), ZoneId.of(timeZone)));
        } catch (DateTimeException ex) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid timeZone. Use an IANA zone such as Asia/Kolkata.");
        }
    }
}
