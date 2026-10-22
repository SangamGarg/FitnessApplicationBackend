package com.fitnessapp.portal.dev.calculatorDev.controller;

import com.fitnessapp.portal.dev.calculatorDev.model.DevCalculatorBurnedCaloriesRequestDto;
import com.fitnessapp.portal.dev.calculatorDev.service.DevCalculatorService;
import com.fitnessapp.portal.dev.calculatorDev.model.DevCalculatorUploadCaloriesInFoodRequestDto;
import com.fitnessapp.portal.dev.shared.DevUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(DevUtils.API_PREFIX_CALCULATOR)
@Tag(name = "Dev Calculator", description = "Endpoints for dev calculator")
public class DevCalculatorController {

    private final DevCalculatorService devCalculatorService;

    @Operation(summary = "Upload calories in food",
            description = "Bulk-uploads calories-in-food items. Every field is required and names must be unique.")
    @PostMapping("/upload-calories-in-food")
    public ResponseEntity<?> uploadCaloriesInFood(
            @RequestBody @NotEmpty(message = "List cannot be empty")
            List<@Valid DevCalculatorUploadCaloriesInFoodRequestDto> requestDto) {

        log.info("POST upload-calories-in-food received: itemCount={}", requestDto.size());
        if (log.isTraceEnabled()) {
            log.trace("Food names in upload payload: {}",
                    requestDto.stream().map(DevCalculatorUploadCaloriesInFoodRequestDto::getName).toList());
        }

        ResponseEntity<?> response = devCalculatorService.uploadCalculatorCaloriesInFood(requestDto);

        log.debug("POST upload-calories-in-food completed: status={}", response.getStatusCode().value());
        return response;
    }

    @Operation(summary = "Patch calories in food",
            description = "Updates the non-null fields of an existing calories-in-food item, matched by name.")
    @PatchMapping("/patch-calories-in-food")
    public ResponseEntity<?> patchCaloriesInFood(
            @RequestBody @Valid DevCalculatorUploadCaloriesInFoodRequestDto requestDto) {

        log.info("PATCH patch-calories-in-food received: name='{}'", requestDto.getName());
        log.trace("Food patch payload: {}", requestDto);

        ResponseEntity<?> response = devCalculatorService.patchCalculatorCaloriesInFood(requestDto);

        log.debug("PATCH patch-calories-in-food completed: name='{}', status={}",
                requestDto.getName(), response.getStatusCode().value());
        return response;
    }

    @Operation(summary = "Upload burned calories",
            description = "Bulk-uploads burned-calories activities. MET value and image URL are required and names must be unique.")
    @PostMapping("/upload-burned-calories")
    public ResponseEntity<?> uploadBurnedCalories(
            @RequestBody @NotEmpty(message = "List cannot be empty")
            List<@Valid DevCalculatorBurnedCaloriesRequestDto> requestDto) {

        log.info("POST upload-burned-calories received: itemCount={}", requestDto.size());
        if (log.isTraceEnabled()) {
            log.trace("Activity names in upload payload: {}",
                    requestDto.stream().map(DevCalculatorBurnedCaloriesRequestDto::getName).toList());
        }

        ResponseEntity<?> response = devCalculatorService.uploadCalculatorBurnedCalories(requestDto);

        log.debug("POST upload-burned-calories completed: status={}", response.getStatusCode().value());
        return response;
    }

    @Operation(summary = "Patch burned calories",
            description = "Updates the non-null fields of an existing burned-calories activity, matched by name.")
    @PatchMapping("/patch-burned-calories")
    public ResponseEntity<?> patchBurnedCalories(
            @RequestBody @Valid DevCalculatorBurnedCaloriesRequestDto requestDto) {

        log.info("PATCH patch-burned-calories received: name='{}'", requestDto.getName());
        log.trace("Burned-calories patch payload: {}", requestDto);

        ResponseEntity<?> response = devCalculatorService.patchCalculatorBurnedCalories(requestDto);

        log.debug("PATCH patch-burned-calories completed: name='{}', status={}",
                requestDto.getName(), response.getStatusCode().value());
        return response;
    }
}
