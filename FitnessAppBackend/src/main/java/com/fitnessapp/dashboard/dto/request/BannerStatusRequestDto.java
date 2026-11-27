package com.fitnessapp.dashboard.dto.request;


import jakarta.validation.constraints.NotNull;

public record BannerStatusRequestDto(
        @NotNull(message = "Active status is required")
        Boolean active
) {
}