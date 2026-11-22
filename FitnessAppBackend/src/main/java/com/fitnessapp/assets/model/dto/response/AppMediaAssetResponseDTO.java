package com.fitnessapp.assets.model.dto.response;

import com.fitnessapp.assets.model.enums.AssetCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppMediaAssetResponseDTO {
    private String assetKey;
    private String title;
    private String imageUrl;
    private String altText;
    private AssetCategory category;
    private String description;
    private LocalDateTime updatedAt;
    @JsonProperty("isActive")
    private boolean isActive;
}