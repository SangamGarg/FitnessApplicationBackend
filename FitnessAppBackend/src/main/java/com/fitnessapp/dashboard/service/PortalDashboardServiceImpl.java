package com.fitnessapp.dashboard.service;

import com.fitnessapp.dashboard.dto.BannerActionType;
import com.fitnessapp.dashboard.dto.entity.AppBannerEntity;
import com.fitnessapp.dashboard.dto.request.BannerReorderRequestDto;
import com.fitnessapp.dashboard.dto.request.BannerStatusRequestDto;
import com.fitnessapp.dashboard.dto.request.BannerUpsertRequestDto;
import com.fitnessapp.dashboard.dto.response.DashboardResponseDto;
import com.fitnessapp.dashboard.repo.AppBannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortalDashboardServiceImpl implements PortalDashboardService {

    private final AppBannerRepository appBannerRepository;

    // --------------------------------------------------
    // GET ALL BANNERS
    // --------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<DashboardResponseDto.AppBannerResponseDto> getAllBanners() {

        log.info("Fetching all dashboard banners");

        return appBannerRepository.findAll()
                .stream()
                .sorted((a, b) -> {
                    int orderCompare = Integer.compare(
                            a.getDisplayOrder(),
                            b.getDisplayOrder()
                    );

                    if (orderCompare != 0) {
                        return orderCompare;
                    }

                    return b.getId().compareTo(a.getId());
                })
                .map(this::toBannerDto)
                .toList();
    }

    // --------------------------------------------------
    // GET BANNER BY ID
    // --------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public DashboardResponseDto.AppBannerResponseDto getBannerById(Long id) {

        log.info("Fetching dashboard banner with ID: {}", id);

        AppBannerEntity banner = getBannerOrThrow(id);

        return toBannerDto(banner);
    }

    // --------------------------------------------------
    // CREATE BANNER
    // --------------------------------------------------

    @Override
    @Transactional
    public DashboardResponseDto.AppBannerResponseDto createBanner(
            BannerUpsertRequestDto requestDto
    ) {

        log.info("Creating dashboard banner with key: {}", requestDto.bannerKey());

        validateBannerRequest(requestDto);

        if (appBannerRepository.existsByBannerKey(requestDto.bannerKey())) {
            throw new IllegalArgumentException(
                    "Banner key already exists: " + requestDto.bannerKey()
            );
        }

        AppBannerEntity banner = new AppBannerEntity();

        applyRequestToEntity(banner, requestDto);

        Instant now = Instant.now();

        banner.setCreatedAt(now);
        banner.setUpdatedAt(now);

        AppBannerEntity savedBanner = appBannerRepository.save(banner);

        log.info(
                "Dashboard banner created successfully. ID: {}, Key: {}",
                savedBanner.getId(),
                savedBanner.getBannerKey()
        );

        return toBannerDto(savedBanner);
    }

    // --------------------------------------------------
    // UPDATE BANNER
    // --------------------------------------------------

    @Override
    @Transactional
    public DashboardResponseDto.AppBannerResponseDto updateBanner(
            Long id,
            BannerUpsertRequestDto requestDto
    ) {

        log.info("Updating dashboard banner with ID: {}", id);

        validateBannerRequest(requestDto);

        AppBannerEntity banner = getBannerOrThrow(id);

        if (appBannerRepository.existsByBannerKeyAndIdNot(
                requestDto.bannerKey(),
                id
        )) {
            throw new IllegalArgumentException(
                    "Banner key already exists: " + requestDto.bannerKey()
            );
        }

        applyRequestToEntity(banner, requestDto);
        banner.setUpdatedAt(Instant.now());

        AppBannerEntity updatedBanner = appBannerRepository.save(banner);

        log.info(
                "Dashboard banner updated successfully. ID: {}",
                updatedBanner.getId()
        );

        return toBannerDto(updatedBanner);
    }

    // --------------------------------------------------
    // DELETE BANNER
    // --------------------------------------------------

    @Override
    @Transactional
    public void deleteBanner(Long id) {

        log.info("Deleting dashboard banner with ID: {}", id);

        AppBannerEntity banner = getBannerOrThrow(id);

        appBannerRepository.delete(banner);

        log.info("Dashboard banner deleted successfully. ID: {}", id);
    }

    // --------------------------------------------------
    // UPDATE BANNER STATUS
    // --------------------------------------------------

    @Override
    @Transactional
    public DashboardResponseDto.AppBannerResponseDto updateBannerStatus(
            Long id,
            BannerStatusRequestDto requestDto
    ) {

        log.info("Updating banner active status. ID: {}", id);

        AppBannerEntity banner = getBannerOrThrow(id);

        banner.setActive(requestDto.active());
        banner.setUpdatedAt(Instant.now());

        AppBannerEntity updatedBanner = appBannerRepository.save(banner);

        log.info(
                "Banner status updated successfully. ID: {}, Active: {}",
                id,
                updatedBanner.isActive()
        );

        return toBannerDto(updatedBanner);
    }

    // --------------------------------------------------
    // REORDER BANNERS
    // --------------------------------------------------

    @Override
    @Transactional
    public void reorderBanners(BannerReorderRequestDto requestDto) {

        log.info("Reordering dashboard banners");

        List<BannerReorderRequestDto.BannerOrderItem> items =
                requestDto.items();

        Set<Long> bannerIds = new HashSet<>();

        for (BannerReorderRequestDto.BannerOrderItem item : items) {

            if (!bannerIds.add(item.id())) {
                throw new IllegalArgumentException(
                        "Duplicate banner ID in reorder request: " + item.id()
                );
            }
        }

        List<AppBannerEntity> banners =
                appBannerRepository.findAllById(bannerIds);

        if (banners.size() != bannerIds.size()) {
            throw new IllegalArgumentException(
                    "One or more banner IDs do not exist"
            );
        }

        for (BannerReorderRequestDto.BannerOrderItem item : items) {

            AppBannerEntity banner = banners.stream()
                    .filter(b -> b.getId().equals(item.id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Banner not found: " + item.id()
                    ));

            banner.setDisplayOrder(item.displayOrder());
            banner.setUpdatedAt(Instant.now());
        }

        appBannerRepository.saveAll(banners);

        log.info(
                "Successfully reordered {} dashboard banners",
                banners.size()
        );
    }

    // --------------------------------------------------
    // ENTITY -> DTO
    // --------------------------------------------------

    private DashboardResponseDto.AppBannerResponseDto toBannerDto(
            AppBannerEntity banner
    ) {

        return new DashboardResponseDto.AppBannerResponseDto(
                banner.getId(),
                banner.getBannerKey(),
                banner.getTitle(),
                banner.getSubTitle(),
                banner.getDescription(),
                banner.getImageUrl(),
                banner.getMobileImageUrl(),
                banner.getActionLabel(),
                banner.getActionType(),
                banner.getActionValue(),
                banner.getDisplayOrder(),
                banner.isDismissible()
        );
    }

    // --------------------------------------------------
    // APPLY DTO -> ENTITY
    // --------------------------------------------------

    private void applyRequestToEntity(
            AppBannerEntity banner,
            BannerUpsertRequestDto requestDto
    ) {

        banner.setBannerKey(requestDto.bannerKey().trim());
        banner.setTitle(requestDto.title().trim());
        banner.setSubTitle(requestDto.subTitle());
        banner.setDescription(requestDto.description());

        banner.setImageUrl(requestDto.imageUrl().trim());
        banner.setImageStorageKey(requestDto.imageStorageKey());

        banner.setMobileImageUrl(requestDto.mobileImageUrl());
        banner.setMobileImageStorageKey(
                requestDto.mobileImageStorageKey()
        );

        banner.setActionType(requestDto.actionType());
        banner.setActionValue(requestDto.actionValue());
        banner.setActionLabel(requestDto.actionLabel());

        banner.setDisplayOrder(requestDto.displayOrder());
        banner.setActive(requestDto.active());

        banner.setStartAt(requestDto.startAt());
        banner.setEndAt(requestDto.endAt());

        banner.setDismissible(requestDto.dismissible());
        banner.setTrackingEnabled(requestDto.trackingEnabled());
    }

    // --------------------------------------------------
    // VALIDATION
    // --------------------------------------------------

    private void validateBannerRequest(BannerUpsertRequestDto requestDto) {

        if (requestDto.startAt() != null
                && requestDto.endAt() != null
                && !requestDto.endAt().isAfter(requestDto.startAt())) {

            throw new IllegalArgumentException(
                    "Banner end time must be after start time"
            );
        }

        if (requestDto.actionType()
                == BannerActionType.NONE
                && requestDto.actionValue() != null
                && !requestDto.actionValue().isBlank()) {

            throw new IllegalArgumentException(
                    "Action value must be empty when action type is NONE"
            );
        }

        if (requestDto.actionType()
                == BannerActionType.WEB_URL) {

            validateWebUrl(requestDto.actionValue());
        }

        if (requestDto.actionType()
                == BannerActionType.DEEPLINK) {

            validateDeepLink(requestDto.actionValue());
        }
    }

    private void validateWebUrl(String url) {

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "URL is required for WEB_URL action"
            );
        }

        if (!url.startsWith("https://")) {
            throw new IllegalArgumentException(
                    "Only HTTPS URLs are allowed for WEB_URL actions"
            );
        }
    }

    private void validateDeepLink(String deepLink) {

        if (deepLink == null || deepLink.isBlank()) {
            throw new IllegalArgumentException(
                    "Deep link is required for DEEPLINK action"
            );
        }

        if (!deepLink.startsWith("fitnessapp://")) {
            throw new IllegalArgumentException(
                    "Unsupported deep link scheme"
            );
        }
    }

    // --------------------------------------------------
    // FIND BANNER
    // --------------------------------------------------

    private AppBannerEntity getBannerOrThrow(Long id) {

        return appBannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Banner not found with ID: " + id
                ));
    }
}