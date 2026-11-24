package com.fitnessapp.analytics.service;

import com.fitnessapp.analytics.model.dtos.requestDto.CalculatorCatalogRequestDto;
import com.fitnessapp.analytics.model.dtos.responseDto.CalculatorCatalogResponseDto;
import com.fitnessapp.analytics.model.dtos.responseDto.CalculatorCatalogItemResponseDto;

import java.util.List;

public interface PortalAnalyticsService {

    List<CalculatorCatalogItemResponseDto> getCalculatorCatalog();

    CalculatorCatalogResponseDto addCalculatorCatalog(CalculatorCatalogRequestDto request);

    CalculatorCatalogResponseDto patchCalculatorCatalog(String key, CalculatorCatalogRequestDto request);

    CalculatorCatalogResponseDto deleteCalculatorCatalog(String key);

}
