package com.fitnessapp.portal.dev.assetUploadDev.client;

import com.fitnessapp.portal.dev.assetUploadDev.repo.AppMediaAssetRepository;
import com.fitnessapp.portal.dev.assetUploadDev.model.AssetCategory;
import com.fitnessapp.portal.dev.assetUploadDev.model.AppMediaAssetEntity;
import com.fitnessapp.portal.dev.assetUploadDev.model.AppMediaAssetResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientMediaAssetServiceImpl implements ClientMediaAssetService {

    private final AppMediaAssetRepository repository;

    @Override
    public AppMediaAssetResponseDTO getActiveAssetByKey(String assetKey) {
        log.debug("Fetching active client asset for key: [{}]", assetKey);

        return repository.findByAssetKeyAndIsActiveTrue(assetKey)
                .map(this::mapToResponse)
                .orElseThrow(() -> {
                    log.warn("Active asset not found for key: [{}]", assetKey);
                    return new IllegalArgumentException("No active asset found with key: " + assetKey);
                });
    }

    @Override
    public List<AppMediaAssetResponseDTO> getActiveAssetsByCategory(AssetCategory category) {
        log.debug("Fetching all active client assets for category: [{}]", category);

        return repository.findByCategoryAndIsActiveTrue(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppMediaAssetResponseDTO> getAllActiveAssets() {
        log.debug("Fetching complete catalog of active client assets");

        return repository.findAllByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AppMediaAssetResponseDTO mapToResponse(AppMediaAssetEntity entity) {
        return AppMediaAssetResponseDTO.builder()
                .assetKey(entity.getAssetKey())
                .title(entity.getTitle())
                .imageUrl(entity.getImageUrl())
                .altText(entity.getAltText())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .build();
    }
}