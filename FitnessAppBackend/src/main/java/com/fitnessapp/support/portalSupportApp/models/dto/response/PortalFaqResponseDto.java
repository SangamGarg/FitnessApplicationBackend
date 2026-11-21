package com.fitnessapp.support.portalSupportApp.models.dto.response;

import com.fitnessapp.support.models.entity.SupportFaqEntity;

import java.time.LocalDateTime;

public record PortalFaqResponseDto(
        Long id,
        Long categoryId,
        String categoryKey,
        String categoryName,
        String question,
        String answer,
        boolean popular,
        boolean active,
        int sortOrder,
        LocalDateTime updatedAt,
        String updatedBy
) {
    public static PortalFaqResponseDto from(SupportFaqEntity faq) {
        return new PortalFaqResponseDto(
                faq.getId(),
                faq.getCategory().getId(),
                faq.getCategory().getCategoryKey(),
                faq.getCategory().getName(),
                faq.getQuestion(),
                faq.getAnswer(),
                faq.isPopular(),
                faq.isActive(),
                faq.getSortOrder(),
                faq.getUpdatedAt(),
                faq.getUpdatedBy()
        );
    }
}
