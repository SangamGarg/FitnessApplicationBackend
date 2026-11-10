package com.fitnessapp.portal.dev.calculatorDev.controller;

import com.fitnessapp.portal.dev.calculatorDev.model.DevCalculatorBurnedCaloriesRequestDto;
import com.fitnessapp.portal.dev.calculatorDev.model.DevCalculatorUploadCaloriesInFoodRequestDto;
import com.fitnessapp.portal.dev.calculatorDev.service.DevCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_CALCULATOR_ENDPOINT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_CALCULATOR_ENDPOINT)
public class DevCalculatorController {

    private final DevCalculatorService devCalculatorService;

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
