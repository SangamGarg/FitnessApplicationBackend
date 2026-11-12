package com.fitnessapp.analytics.service;

import com.fitnessapp.analytics.model.dtos.responseDto.AnalyticsHomeResponseDto;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {

    AnalyticsHomeResponseDto getAnalyticsHome();

    List<Map<String, Object>> getCatalog();

    Map<String, Object> calculate(String key, Map<String, Object> in);

    Map<String, Object> getFitnessScore(ZoneId zoneId);
}