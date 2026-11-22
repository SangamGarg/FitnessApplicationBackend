package com.fitnessapp.analytics.service;

import com.fitnessapp.analytics.model.dtos.requestDto.CalculatorCatalogRequestDto;
import com.fitnessapp.analytics.model.dtos.responseDto.AnalyticsHomeResponseDto;
import com.fitnessapp.analytics.model.dtos.responseDto.CalculatorCatalogResponseDto;
import com.fitnessapp.analytics.model.dtos.responseDto.CalculatorHomeResponseDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {

    AnalyticsHomeResponseDto getAnalyticsHome();

    CalculatorHomeResponseDto getCalculatorHome();

    List<Map<String, Object>> getCatalog();

    Map<String, Object> calculate(String key, Map<String, Object> in);

    Map<String, Object> getFitnessScore(ZoneId zoneId);


    CalculatorCatalogResponseDto addCatalog(CalculatorCatalogRequestDto request);

    CalculatorCatalogResponseDto patchCatalog(String key, CalculatorCatalogRequestDto request);
}