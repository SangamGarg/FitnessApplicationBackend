package com.fitnessapp.dashboard.dto.request;

import com.fitnessapp.dashboard.dto.BannerActionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record BannerUpsertRequestDto(

        @NotBlank
        @Size(max = 100)
        String bannerKey,

        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 250)
        String subTitle,

        @Size(max = 1000)
        String description,

        @NotBlank
        String imageUrl,

        String imageStorageKey,

        String mobileImageUrl,

        String mobileImageStorageKey,

        @NotNull
        BannerActionType actionType,

        @Size(max = 500)
        String actionValue,

        @Size(max = 150)
        String actionLabel,

        @NotNull
        @Min(0)
        Integer displayOrder,

        boolean active,

        Instant startAt,

        Instant endAt,

        boolean dismissible,

        boolean trackingEnabled
) {}