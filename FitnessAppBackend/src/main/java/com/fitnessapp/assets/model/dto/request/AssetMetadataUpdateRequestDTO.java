package com.fitnessapp.assets.model.dto.request;

import com.fitnessapp.assets.model.enums.AssetCategory;
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