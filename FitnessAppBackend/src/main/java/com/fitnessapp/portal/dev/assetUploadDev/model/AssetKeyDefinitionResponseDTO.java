package com.fitnessapp.portal.dev.assetUploadDev.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetKeyDefinitionResponseDTO {
    private String assetKey;
    private String title;
    private AssetCategory category;
    private String altText;
    private String description;
    private boolean uploaded;
}
