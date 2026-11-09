package com.fitnessapp.portal.dev.assetUploadDev.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssetUploadRequestDTO {

    @NotBlank(message = "Asset key is required")
    @Size(max = 100, message = "Asset key must not exceed 100 characters")
    private String assetKey;

    // Optional overrides; registered catalog metadata is used when omitted.
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    private AssetCategory category;

    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    private String altText;

    private String description;
}