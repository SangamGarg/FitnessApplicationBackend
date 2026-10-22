package com.fitnessapp.portal.dev.assetUploadDev.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetMetadataUpdateRequestDTO {
    private String title;
    private String altText;
    private AssetCategory category;
    private String description;
}