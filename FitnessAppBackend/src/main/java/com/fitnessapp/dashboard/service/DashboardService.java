package com.fitnessapp.dashboard.service;

import com.fitnessapp.dashboard.dto.response.DashboardResponseDto;

public interface DashboardService {
    DashboardResponseDto getDashboard(String timeZone);
}
