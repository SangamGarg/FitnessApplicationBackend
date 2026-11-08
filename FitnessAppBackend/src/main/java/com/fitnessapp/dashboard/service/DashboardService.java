package com.fitnessapp.dashboard.service;

import com.fitnessapp.dashboard.dto.DashboardResponseDto;
import org.springframework.http.ResponseEntity;

public interface DashboardService {

    ResponseEntity<DashboardResponseDto> getDashboard(String timeZone);
}
