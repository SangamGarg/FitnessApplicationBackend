package com.fitnessapp.portal.dev.imagesUploadDev.models;


import com.fitnessapp.portal.dev.imagesUploadDev.AssetCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssetUploadRequestDTO {

    @NotBlank(message = "Asset key is required")
    private String assetKey;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Category is required")
    private AssetCategory category;

    private String altText;
    private String description;
}