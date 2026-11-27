package com.fitnessapp.dashboard.service;

import com.fitnessapp.dashboard.dto.request.BannerReorderRequestDto;
import com.fitnessapp.dashboard.dto.request.BannerStatusRequestDto;
import com.fitnessapp.dashboard.dto.request.BannerUpsertRequestDto;
import com.fitnessapp.dashboard.dto.response.DashboardResponseDto;

import java.util.List;

public interface PortalDashboardService {

    List<DashboardResponseDto.AppBannerResponseDto> getAllBanners();

    DashboardResponseDto.AppBannerResponseDto getBannerById(Long id);

    DashboardResponseDto.AppBannerResponseDto createBanner(
            BannerUpsertRequestDto requestDto
    );

    DashboardResponseDto.AppBannerResponseDto updateBanner(
            Long id,
            BannerUpsertRequestDto requestDto
    );

    void deleteBanner(Long id);

    DashboardResponseDto.AppBannerResponseDto updateBannerStatus(
            Long id,
            BannerStatusRequestDto requestDto
    );

    void reorderBanners(BannerReorderRequestDto requestDto);
}