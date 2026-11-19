package com.fitnessapp.dashboard.controller;

import com.fitnessapp.dashboard.dto.DashboardResponseDto;
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
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDto> getDashboard(
            @RequestParam(required = false) String timeZone
    ) {
        log.info("Received request for dashboard");
        ResponseEntity<DashboardResponseDto> response = dashboardService.getDashboard(timeZone);
        log.info("Dashboard request completed with status: {}", response.getStatusCode());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().cachePrivate())
                .header("X-Dashboard-Generated-At", response.getBody().generatedAt().toString())
                .body(response.getBody());
    }
}
