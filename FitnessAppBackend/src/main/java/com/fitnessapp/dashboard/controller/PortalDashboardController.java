package com.fitnessapp.dashboard.controller;

import com.fitnessapp.dashboard.dto.request.BannerReorderRequestDto;
import com.fitnessapp.dashboard.dto.request.BannerStatusRequestDto;
import com.fitnessapp.dashboard.dto.request.BannerUpsertRequestDto;
import com.fitnessapp.dashboard.dto.response.DashboardResponseDto;
import com.fitnessapp.dashboard.service.PortalDashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_DASHBOARD_ENDPOINT;

@RestController
@RequestMapping(PORTAL_DEV_DASHBOARD_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
public class PortalDashboardController {

    private final PortalDashboardService portalDashboardService;

    @GetMapping("/banners")
    public ResponseEntity<List<DashboardResponseDto.AppBannerResponseDto>> getAllBanners() {

        log.info("REST request to fetch all dashboard banners");

        return ResponseEntity.ok(
                portalDashboardService.getAllBanners()
        );
    }

    @GetMapping("/banners/{id}")
    public ResponseEntity<DashboardResponseDto.AppBannerResponseDto> getBannerById(@PathVariable Long id) {
        log.info("REST request to fetch dashboard banner ID: {}", id);

        return ResponseEntity.ok(
                portalDashboardService.getBannerById(id)
        );
    }

    @PostMapping("/banners")
    public ResponseEntity<DashboardResponseDto.AppBannerResponseDto> createBanner(@Valid @RequestBody BannerUpsertRequestDto requestDto) {

        log.info("REST request to create dashboard banner");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(portalDashboardService.createBanner(requestDto));
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<DashboardResponseDto.AppBannerResponseDto> updateBanner(@PathVariable Long id, @Valid @RequestBody BannerUpsertRequestDto requestDto) {

        log.info("REST request to update dashboard banner ID: {}", id);

        return ResponseEntity.ok(
                portalDashboardService.updateBanner(id, requestDto)
        );
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {

        log.info("REST request to delete dashboard banner ID: {}", id);

        portalDashboardService.deleteBanner(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/banners/{id}/status")
    public ResponseEntity<DashboardResponseDto.AppBannerResponseDto> updateBannerStatus(@PathVariable Long id, @Valid @RequestBody BannerStatusRequestDto requestDto) {

        log.info("REST request to update banner status ID: {}", id);

        return ResponseEntity.ok(
                portalDashboardService.updateBannerStatus(id, requestDto)
        );
    }

    @PutMapping("/banners/reorder")
    public ResponseEntity<Void> reorderBanners(@Valid @RequestBody BannerReorderRequestDto requestDto) {

        log.info("REST request to reorder dashboard banners");

        portalDashboardService.reorderBanners(requestDto);

        return ResponseEntity.noContent().build();
    }
}