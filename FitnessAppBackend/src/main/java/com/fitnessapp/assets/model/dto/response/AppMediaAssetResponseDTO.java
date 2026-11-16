package com.fitnessapp.assets.model.dto.response;

import com.fitnessapp.assets.model.enums.AssetCategory;
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