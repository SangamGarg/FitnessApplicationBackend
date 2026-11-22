package com.fitnessapp.assets.model.dto.response;

import com.fitnessapp.assets.model.enums.AssetCategory;
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
    private String imageUrl;
    private boolean enabled;
}
