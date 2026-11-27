package com.fitnessapp.dashboard.controller;

import com.fitnessapp.dashboard.dto.response.DashboardResponseDto;
import com.fitnessapp.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_DASHBOARD_ENDPOINT;

@RestController
@RequestMapping(APP_DASHBOARD_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
public class AppDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDto> getDashboard(
            @RequestParam(required = false) String timeZone
    ) {
        log.info("Received request for dashboard");
        DashboardResponseDto response = dashboardService.getDashboard(timeZone);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().cachePrivate())
                .header("X-Dashboard-Generated-At", response.generatedAt().toString())
                .body(response);
    }
}
