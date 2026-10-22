package com.fitnessapp.portal.dev.assetUploadDev.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssetKeyDefinitionRequestDTO {
    @NotBlank
    @Size(max = 100)
    private String assetKey;

    @NotBlank
    @Size(max = 150)
    private String title;

    @NotNull
    private AssetCategory category;

    @Size(max = 255)
    private String altText;

    private String description;
}
