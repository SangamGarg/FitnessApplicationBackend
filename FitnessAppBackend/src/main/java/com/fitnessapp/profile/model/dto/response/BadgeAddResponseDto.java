package com.fitnessapp.profile.model.dto.response;

public record BadgeAddResponseDto(BadgeResponseDto badge, String message, Long badgeId) {
    public BadgeAddResponseDto(BadgeResponseDto badge, String message) {
        this(badge, message, badge == null ? null : badge.id());
    }
}
