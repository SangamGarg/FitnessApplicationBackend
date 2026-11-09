package com.fitnessapp.portal.dev.assetUploadDev.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppMediaAssetResponseDTO {
    private String assetKey;
    private String title;
    private String imageUrl;
    private String altText;
    private AssetCategory category;
    private String description;
}