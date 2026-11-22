package com.fitnessapp.assets.util;

import com.fitnessapp.assets.model.dto.response.AppMediaAssetResponseDTO;
import com.fitnessapp.assets.model.dto.response.AssetKeyDefinitionResponseDTO;
import com.fitnessapp.assets.model.entity.AppMediaAssetEntity;
import com.fitnessapp.assets.model.enums.AssetStatus;

public final class AssetMapper {
    private AssetMapper() {
    }

    public static AppMediaAssetResponseDTO toAssetResponse(AppMediaAssetEntity entity) {
        return AppMediaAssetResponseDTO.builder()
                .assetKey(entity.getAssetKey())
                .title(entity.getTitle())
                .imageUrl(entity.getImageUrl())
                .altText(entity.getAltText())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .updatedAt(entity.getUpdatedAt())
                .isActive(entity.getStatus() == AssetStatus.ACTIVE)
                .build();
    }

    public static AssetKeyDefinitionResponseDTO toKeyResponse(AppMediaAssetEntity entity) {
        return AssetKeyDefinitionResponseDTO.builder()
                .assetKey(entity.getAssetKey())
                .title(entity.getTitle())
                .category(entity.getCategory())
                .altText(entity.getAltText())
                .description(entity.getDescription())
                .uploaded(entity.getImageUrl() != null)
                .imageUrl(entity.getImageUrl())
                .enabled(entity.getStatus() != AssetStatus.DISABLED)
                .build();
    }
}
