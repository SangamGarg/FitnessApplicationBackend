package com.fitnessapp.dashboard.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import java.util.List;

public record BannerReorderRequestDto(

        @NotEmpty(message = "Banner reorder list cannot be empty")
        List<@Valid BannerOrderItem> items

) {

    public record BannerOrderItem(

            @NotNull(message = "Banner ID is required")
            Long id,

            @NotNull(message = "Display order is required")
            @Min(0)
            Integer displayOrder

    ) {
    }
}