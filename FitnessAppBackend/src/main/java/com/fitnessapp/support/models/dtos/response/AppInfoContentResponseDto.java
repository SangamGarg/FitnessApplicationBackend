package com.fitnessapp.support.models.dtos.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AppInfoContentResponseDto(
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long version,
        String updatedBy
) {
}