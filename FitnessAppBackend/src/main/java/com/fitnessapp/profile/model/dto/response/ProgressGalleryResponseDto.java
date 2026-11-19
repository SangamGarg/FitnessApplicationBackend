package com.fitnessapp.profile.model.dto.response;

import com.fitnessapp.profile.model.entity.ProgressGalleryEntity;

import java.time.Instant;

public record ProgressGalleryResponseDto(
        Long id,
        String imageUrl,
        String caption,
        Instant uploadedAt,
        String category
) {
    public static ProgressGalleryResponseDto from(ProgressGalleryEntity entity) {
        return new ProgressGalleryResponseDto(
                entity.getId(),
                entity.getImageUrl(),
                entity.getCaption(),
                entity.getUploadedAt(),
                entity.getCategory()
        );
    }
}
